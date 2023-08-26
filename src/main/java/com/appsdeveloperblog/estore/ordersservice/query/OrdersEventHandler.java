package com.appsdeveloperblog.estore.ordersservice.query;

import com.appsdeveloperblog.estore.ordersservice.core.data.OrderEntity;
import com.appsdeveloperblog.estore.ordersservice.core.data.OrdersRepository;
import com.appsdeveloperblog.estore.ordersservice.core.events.OrderApprovedEvent;
import com.appsdeveloperblog.estore.ordersservice.core.events.OrderCreatedEvent;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.axonframework.messaging.interceptors.ExceptionHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup("order-group")
public class OrdersEventHandler {

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

        OrderEntity orderEntity = ordersRepository.findByOrderId(event.getOrderId());
        if(orderEntity==null){
            return;
        }
        orderEntity.setOrderStatus(event.getOrderStatus());
        ordersRepository.save(orderEntity);

    }
}
