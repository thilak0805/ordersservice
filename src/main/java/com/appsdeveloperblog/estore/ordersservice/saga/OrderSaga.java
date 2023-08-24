package com.appsdeveloperblog.estore.ordersservice.saga;

import com.appsdeveloperblog.estore.core.commands.ProcessPaymentCommand;
import com.appsdeveloperblog.estore.core.commands.ReserveProductCommand;
import com.appsdeveloperblog.estore.core.events.PaymentProcessedEvent;
import com.appsdeveloperblog.estore.core.events.ProductReservedEvent;
import com.appsdeveloperblog.estore.core.model.User;
import com.appsdeveloperblog.estore.core.query.FetchUserPaymentDetailsQuery;
import com.appsdeveloperblog.estore.ordersservice.command.ApproveOrderCommand;
import com.appsdeveloperblog.estore.ordersservice.core.events.OrderCreatedEvent;
import org.axonframework.commandhandling.CommandCallback;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.commandhandling.CommandResultMessage;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.modelling.saga.SagaEventHandler;
import org.axonframework.modelling.saga.StartSaga;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.spring.stereotype.Saga;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Saga
public class OrderSaga {
    Logger logger = LoggerFactory.getLogger("OrderSaga.class");

    @Autowired
    private transient CommandGateway commandGateway;

    @Autowired
    private transient QueryGateway queryGateway;

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
        }
        if(userPaymentDetails == null){
            //start compensating transaction
            return;
        }else{
            logger.info("Successfully fetched user payment details for user id "+userPaymentDetails.getFirstName());
            ProcessPaymentCommand processPaymentCommand = ProcessPaymentCommand.builder()
                    .orderId(productReservedEvent.getOrderId())
                    .paymentDetails(userPaymentDetails.getPaymentDetails())
                    .paymentId(UUID.randomUUID().toString())
                    .build();
            String result = null;
            try {
                //result = commandGateway.sendAndWait(processPaymentCommand, 10, TimeUnit.SECONDS);
                //result = commandGateway.send(processPaymentCommand);
                commandGateway.send(processPaymentCommand, new CommandCallback<ProcessPaymentCommand, Object>(){
                    @Override
                    public void onResult(CommandMessage<? extends ProcessPaymentCommand> commandMessage, CommandResultMessage<?> commandResultMessage) {
                        if(commandResultMessage.isExceptional()){
                            logger.info("starting compensating transaction for process payment command");

                        }
                    }
                });

            }catch(Exception e){
                e.printStackTrace();
            }

        }
    }


    @SagaEventHandler(associationProperty = "orderId")
    public void handle(PaymentProcessedEvent paymentProcessedEvent){
        logger.info("handling payment processed event");
        ApproveOrderCommand approveOrderCommand = new ApproveOrderCommand(paymentProcessedEvent.getOrderId());
        //use command gateway to send command obj to command bus. command bus routes the request to command handler
        commandGateway.send(approveOrderCommand);
    }
}
