package com.appsdeveloperblog.estore.ordersservice.command.rest;

import com.appsdeveloperblog.estore.ordersservice.command.ApproveOrderCommand;
import com.appsdeveloperblog.estore.ordersservice.command.CreateOrderCommand;
import com.appsdeveloperblog.estore.ordersservice.command.OrderStatus;
import com.appsdeveloperblog.estore.ordersservice.command.interceptor.CreateOrderCommandInterceptor;
import com.appsdeveloperblog.estore.ordersservice.core.events.OrderApprovedEvent;
import com.appsdeveloperblog.estore.ordersservice.core.events.OrderCreatedEvent;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

@Aggregate
public class OrderAggregate {
    Logger logger = LoggerFactory.getLogger(OrderAggregate.class);
    @AggregateIdentifier
    private String orderId;
    private String productId;
    private String userId;
    private int quantity;
    private String addressId;
    private OrderStatus orderStatus;

    public OrderAggregate(){

    }

    @CommandHandler
    public OrderAggregate(CreateOrderCommand createOrderCommand) throws Exception {
        logger.info("inside order aggregate============");
        OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent();
        BeanUtils.copyProperties(createOrderCommand , orderCreatedEvent);
        AggregateLifecycle.apply(orderCreatedEvent);
       // if(true) throw new Exception("An error took place in the create order command in commandhandler method");

    }

    @EventSourcingHandler
    public void on(OrderCreatedEvent orderCreatedEvent) throws Exception{
        logger.info("orcercreateevent has been triggerred from orderaggregate class");
        this.orderId = orderCreatedEvent.getOrderId();
        this.productId = orderCreatedEvent.getProductId();
        this.userId = orderCreatedEvent.getUserId();
        this.addressId = orderCreatedEvent.getAddressId();
        this.quantity = orderCreatedEvent.getQuantity();
        this.orderStatus = orderCreatedEvent.getOrderStatus();

    }

    @CommandHandler
    public void handle(ApproveOrderCommand approveOrderCommand){
        logger.info("Handling approveOrderCommand=============");
        OrderApprovedEvent orderApprovedEvent = new OrderApprovedEvent(approveOrderCommand.getOrderId());
        AggregateLifecycle.apply(orderApprovedEvent);
    }

    @EventSourcingHandler
    public void on(OrderApprovedEvent orderApprovedEvent){
        logger.info("OrderApprovedEvent triggered=========");
        this.orderStatus = orderApprovedEvent.getOrderStatus();
    }



}
