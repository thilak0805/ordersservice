package com.appsdeveloperblog.estore.ordersservice.query;

import com.appsdeveloperblog.estore.ordersservice.command.rest.OrderAggregate;
import com.appsdeveloperblog.estore.ordersservice.core.data.OrderEntity;
import com.appsdeveloperblog.estore.ordersservice.core.data.OrdersRepository;
import com.appsdeveloperblog.estore.ordersservice.core.events.OrderApprovedEvent;
import com.appsdeveloperblog.estore.ordersservice.core.events.OrderCreatedEvent;
import com.appsdeveloperblog.estore.ordersservice.core.events.OrderRejectedEvent;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.messaging.interceptors.ExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup("order-group")
public class OrdersEventHandler {
    Logger logger = LoggerFactory.getLogger(OrdersEventHandler.class);
    private final OrdersRepository ordersRepository;

    public OrdersEventHandler(OrdersRepository ordersRepository) {
        this.ordersRepository = ordersRepository;
    }

    @EventHandler
    public void on(OrderCreatedEvent event) throws Exception {
        System.out.println("inside ordereventhandler method===="+event.getOrderId());
        OrderEntity orderEntity = new OrderEntity();
        BeanUtils.copyProperties(event, orderEntity);
        System.out.println("before saving the order entity");
        try{
            ordersRepository.save(orderEntity);
        }catch (IllegalArgumentException e){
            e.printStackTrace();
        }
       // if(true) throw new Exception("Forcing exception in the order event handler class");
        System.out.println("after saving the order entity");
    }

    @ExceptionHandler(resultType = IllegalArgumentException.class)
    public void handle(IllegalArgumentException exception){
        throw exception;
    }

    @EventHandler
    public void on(OrderApprovedEvent event) throws Exception{
        logger.info("inside OrdersEventHandler class order approved event====={}",event.getOrderId());
        OrderEntity orderEntity = ordersRepository.findByOrderId(event.getOrderId());
        logger.info("orderEntity====={}",orderEntity);
        if(orderEntity==null){
            logger.info("orderentity is null====");
            return;
        }
        try {
            orderEntity.setOrderStatus(event.getOrderStatus());
            ordersRepository.save(orderEntity);
        }catch(Exception e){
            e.printStackTrace();
        }

    }

    @EventHandler
    public void on(OrderRejectedEvent orderRejectedEvent){
        OrderEntity orderEntity = ordersRepository.findByOrderId(orderRejectedEvent.getOrderId());
        orderEntity.setOrderStatus(orderRejectedEvent.getOrderStatus());
        ordersRepository.save(orderEntity);
    }
}
