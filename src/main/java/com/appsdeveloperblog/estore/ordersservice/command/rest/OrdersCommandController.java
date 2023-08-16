package com.appsdeveloperblog.estore.ordersservice.command.rest;

import com.appsdeveloperblog.estore.ordersservice.command.CreateOrderCommand;
import com.appsdeveloperblog.estore.ordersservice.command.OrderStatus;

import org.axonframework.commandhandling.gateway.CommandGateway;
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

    @Autowired
    public OrdersCommandController(CommandGateway commandGateway){
        this.commandGateway = commandGateway;
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
}
