package com.appsdeveloperblog.estore.ordersservice.command;

import com.appsdeveloperblog.estore.ordersservice.core.data.OrderLookupEntity;
import com.appsdeveloperblog.estore.ordersservice.core.data.OrderLookupRepository;
import com.appsdeveloperblog.estore.ordersservice.core.events.OrderCreatedEvent;
import org.axonframework.config.ProcessingGroup;
import org.axonframework.eventhandling.EventHandler;
import org.hibernate.criterion.Order;
import org.springframework.stereotype.Component;

@Component
@ProcessingGroup("order-group")
public class OrdersLookupEventsHandler {

    private final OrderLookupRepository orderLookupRepository;

    public OrdersLookupEventsHandler(OrderLookupRepository orderLookupRepository){
        this.orderLookupRepository = orderLookupRepository;
    }

    @EventHandler
    public void on(OrderCreatedEvent event){
        OrderLookupEntity orderLookupEntity = new OrderLookupEntity(event.getOrderId(),event.getProductId(),event.getAddressId());
        orderLookupRepository .save(orderLookupEntity);
    }
}
