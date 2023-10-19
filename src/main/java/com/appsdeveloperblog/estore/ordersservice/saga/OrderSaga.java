package com.appsdeveloperblog.estore.ordersservice.saga;

import com.appsdeveloperblog.estore.core.commands.CancelProductReservationCommand;
import com.appsdeveloperblog.estore.core.commands.ProcessPaymentCommand;
import com.appsdeveloperblog.estore.core.commands.ReserveProductCommand;
import com.appsdeveloperblog.estore.core.events.PaymentProcessedEvent;
import com.appsdeveloperblog.estore.core.events.ProductReservationCancelledEvent;
import com.appsdeveloperblog.estore.core.events.ProductReservedEvent;
import com.appsdeveloperblog.estore.core.model.OrderSummary;
import com.appsdeveloperblog.estore.core.model.User;
import com.appsdeveloperblog.estore.core.query.FetchUserPaymentDetailsQuery;
import com.appsdeveloperblog.estore.ordersservice.command.ApproveOrderCommand;
import com.appsdeveloperblog.estore.ordersservice.command.RejectOrderCommand;
import com.appsdeveloperblog.estore.ordersservice.core.events.OrderApprovedEvent;
import com.appsdeveloperblog.estore.ordersservice.core.events.OrderCreatedEvent;
import com.appsdeveloperblog.estore.ordersservice.core.events.OrderRejectedEvent;
import com.appsdeveloperblog.estore.ordersservice.query.FindOrdersQuery;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.CommandResultMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.deadline.annotation.DeadlineHandler;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.modelling.saga.EndSaga;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.SagaLifecycle;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.QueryUpdateEmitter;
import org.axonframework.spring.stereotype.Saga;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Saga
public class OrderSaga {
    Logger logger = LoggerFactory.getLogger("OrderSaga.class");

    private final String PAYMENT_PROCESSING_TIMEOUT_DEADLINE="payment-processing-deadline";

    private String scheduleId;

    @Autowired
    private transient CommandGateway commandGateway;

    @Autowired
    private transient QueryGateway queryGateway;

    @Autowired
    private transient DeadlineManager deadlineManager;

    @Autowired
    private transient QueryUpdateEmitter queryUpdateEmitter;

    @StartSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderCreatedEvent orderCreatedEvent){
            ReserveProductCommand reserveProductCommand = ReserveProductCommand.builder()
                .orderId(orderCreatedEvent.getOrderId())
                .productId(orderCreatedEvent.getProductId())
                .quantity(orderCreatedEvent.getQuantity())
                .userId(orderCreatedEvent.getUserId())
                .build();
        logger.info("ordercreatedevent handled for orderId :"+reserveProductCommand.getOrderId()+" and productId :"+reserveProductCommand.getProductId());
        commandGateway.send(reserveProductCommand, new CommandCallback<ReserveProductCommand, Object>() {
            @Override
            public void onResult(CommandMessage<? extends ReserveProductCommand> commandMessage, CommandResultMessage<?> commandResultMessage) {
                if(commandResultMessage.isExceptional()){
                    //start compensating transaction here
                    RejectOrderCommand rejectOrderCommand = new RejectOrderCommand(orderCreatedEvent.getOrderId(),
                            orderCreatedEvent.getReason());
                    commandGateway.send(rejectOrderCommand);
                }
            }
        });

    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(ProductReservedEvent productReservedEvent){
        logger.info("product reserved event is called for orderId :"+productReservedEvent.getOrderId()+" and productId :"+productReservedEvent.getProductId());
        logger.info("product reserved userId:"+productReservedEvent.getUserId());
        //process User payment details
        FetchUserPaymentDetailsQuery fetchUserPaymentDetailsQuery = new FetchUserPaymentDetailsQuery(productReservedEvent.getUserId());
        User userPaymentDetails= null;
        try {
            userPaymentDetails = queryGateway.query(fetchUserPaymentDetailsQuery, ResponseTypes.instanceOf(User.class)).join();
        }catch (Exception e){
            //e.printStackTrace();
            //initiate cancelledReservationProductReservedEvent
            cancelProductReservation(productReservedEvent, e.getMessage());
        }
        if(userPaymentDetails == null){
            //start compensating transaction
            //initiate cancelledReservationProductReservedEvent
            cancelProductReservation(productReservedEvent, "could not fetch user payment details");
            return;
        }else{
            logger.info("Successfully fetched user payment details for user id "+userPaymentDetails.getFirstName());
            // schedule a dealine manager
           // scheduleId = deadlineManager.schedule(Duration.of(10, ChronoUnit.SECONDS),
             //       PAYMENT_PROCESSING_TIMEOUT_DEADLINE, productReservedEvent);
            //to replicate the deadline issue below code added
           //  if(true) return;
            
            ProcessPaymentCommand processPaymentCommand = ProcessPaymentCommand.builder()
                    .orderId(productReservedEvent.getOrderId())
                    .paymentDetails(userPaymentDetails.getPaymentDetails())
                    .paymentId(UUID.randomUUID().toString())
                    .build();
            String result = null;
            try {
                //result = commandGateway.sendAndWait(processPaymentCommand, 10, TimeUnit.SECONDS);
                result = commandGateway.send(processPaymentCommand).toString();
                /*commandGateway.send(processPaymentCommand, new CommandCallback<ProcessPaymentCommand, Object>(){
                    @Override
                    public void onResult(CommandMessage<? extends ProcessPaymentCommand> commandMessage, CommandResultMessage<?> commandResultMessage) {
                        if(commandResultMessage.isExceptional()){
                            logger.info("starting compensating transaction for process payment command");

                        }
                    }
                });*/

            }catch(Exception e){
                e.printStackTrace();
            }
            if(result==null){
                cancelProductReservation(productReservedEvent,"Could not process user payment with provided User Details");
            }

        }
    }


    @SagaEventHandler(associationProperty = "orderId")
    public void handle(PaymentProcessedEvent paymentProcessedEvent){
        //cancelling deadline
        cancelDeadline();
        logger.info("handling payment processed event");
        ApproveOrderCommand approveOrderCommand = new ApproveOrderCommand(paymentProcessedEvent.getOrderId());
        //use command gateway to send command obj to command bus. command bus routes the request to command handler
        commandGateway.send(approveOrderCommand);

    }


    @SagaEventHandler(associationProperty = "orderId")
    public void handle(ProductReservationCancelledEvent productReservationCancelledEvent){
        //create and send reject order command
        RejectOrderCommand rejectOrderCommand = new RejectOrderCommand(
                productReservationCancelledEvent.getOrderId(),
                productReservationCancelledEvent.getReason());
        commandGateway.send(rejectOrderCommand);
    }

    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderRejectedEvent orderRejectedEvent){
        logger.info("Successfully rejected order ",orderRejectedEvent.getOrderId());
        queryUpdateEmitter.emit(FindOrdersQuery.class, query->true, new OrderSummary(orderRejectedEvent.getOrderId(),
                orderRejectedEvent.getOrderStatus(), orderRejectedEvent.getReason()));
    }

    @EndSaga
    @SagaEventHandler(associationProperty = "orderId")
    public void handle(OrderApprovedEvent orderApprovedEvent){
        logger.info("order is approved. order saga is complete for orderId==={}",orderApprovedEvent.getOrderId());
        try {
            logger.info("calling queryUpdateEmitter before");
            queryUpdateEmitter.emit(FindOrdersQuery.class, query -> true,
                    new OrderSummary(orderApprovedEvent.getOrderId(), orderApprovedEvent.getOrderStatus(), "Approved"));
            logger.info("calling queryUpdateEmitter after");
        }catch (Exception e){
            e.printStackTrace();
        }
        //SagaLifecycle.end();
    }


    private void cancelProductReservation(ProductReservedEvent productReservedEvent, String reason){
        cancelDeadline();
        CancelProductReservationCommand cancelProductReservationCommand =  CancelProductReservationCommand
                .builder()
                .orderId(productReservedEvent.getOrderId())
                .productId(productReservedEvent.getProductId())
                .quantity(productReservedEvent.getQuantity())
                .userId(productReservedEvent.getUserId())
                .reason(reason).build();
        commandGateway.send(cancelProductReservationCommand);

    }

    @DeadlineHandler(deadlineName = PAYMENT_PROCESSING_TIMEOUT_DEADLINE)
    public void handlePaymentDeadline(ProductReservedEvent productReservedEvent){
        logger.info("Payment processing deadline took place, sending a compensating command to cancel the product reservation");
        cancelProductReservation(productReservedEvent, "Payment timeout");
    }

    private void cancelDeadline() {
        if(scheduleId!=null) {
            deadlineManager.cancelAll("payment-processing-deadline");
        }
    }

}
