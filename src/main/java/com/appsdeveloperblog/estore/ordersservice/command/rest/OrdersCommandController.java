package com.appsdeveloperblog.estore.ordersservice.command.rest;

import com.appsdeveloperblog.estore.core.model.OrderSummary;
import com.appsdeveloperblog.estore.ordersservice.command.CreateOrderCommand;
import com.appsdeveloperblog.estore.ordersservice.command.OrderStatus;

import com.appsdeveloperblog.estore.ordersservice.query.FindOrdersQuery;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.axonframework.queryhandling.SubscriptionQueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@RequestMapping("/orders")
public class OrdersCommandController {
    Logger logger = LoggerFactory.getLogger(OrdersCommandController.class);

    private final CommandGateway commandGateway;

    private final QueryGateway queryGateway;

    @Autowired
    public OrdersCommandController(CommandGateway commandGateway, QueryGateway queryGateway){
        this.commandGateway = commandGateway;
        this.queryGateway = queryGateway;
    }

    @PostMapping
    public String createOrders(@Valid @RequestBody CreateOrdersRestModel ordersRestModel){
        String returnValue = "";
        CreateOrderCommand createOrderCommand = CreateOrderCommand.builder()
                .orderId(UUID.randomUUID().toString())
                .orderStatus(OrderStatus.CREATED)
                .userId(UUID.randomUUID().toString())
                .addressId(ordersRestModel.getAddressId())
                .productId(ordersRestModel.getProductId())
                .quantity(ordersRestModel.getQuantity())
                .build();

        //command gateway sends the order object to command bus and command bus routes the order object to command handler
     //   try {
        logger.info("before commandgateway sends the createordercommand to commanbus");
            returnValue = commandGateway.sendAndWait(createOrderCommand);
        logger.info("after commandgateway sends the createordercommand to commanbus");
      /*  }catch (Exception e){
            returnValue = e.getLocalizedMessage();
        }*/
        return returnValue;
    }
    // modifiying for subscription query
    /*@PostMapping
    public OrderSummary createOrders(@Valid @RequestBody CreateOrdersRestModel ordersRestModel){
        String returnValue = "";
        String orderId = UUID.randomUUID().toString();
        CreateOrderCommand createOrderCommand = CreateOrderCommand.builder()
                .orderId(UUID.randomUUID().toString())
                .orderStatus(OrderStatus.CREATED)
                .userId(UUID.randomUUID().toString())
                .addressId(ordersRestModel.getAddressId())
                .productId(ordersRestModel.getProductId())
                .quantity(ordersRestModel.getQuantity())
                .build();

        //query gateway
        logger.info("before calling subscription query");
        SubscriptionQueryResult<OrderSummary, OrderSummary> queryResult = queryGateway.subscriptionQuery(new FindOrdersQuery(orderId), ResponseTypes.instanceOf(OrderSummary.class), ResponseTypes.instanceOf(OrderSummary.class));
        logger.info("after calling subscription query");
        //command gateway sends the order object to command bus and command bus routes the order object to command handler
           try {
       // logger.info("before commandgateway sends the createordercommand to commanbus");
        //returnValue = commandGateway.sendAndWait(createOrderCommand);
        //logger.info("after commandgateway sends the createordercommand to commanbus");
               commandGateway.sendAndWait(createOrderCommand);

        }catch (Exception e){
            returnValue = e.getLocalizedMessage();
        }finally {
               queryResult.close();
           }
       // return returnValue;
        return queryResult.updates().blockFirst();
    }*/
}
