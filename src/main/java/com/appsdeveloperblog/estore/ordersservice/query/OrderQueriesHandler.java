package com.appsdeveloperblog.estore.ordersservice.query;

import com.appsdeveloperblog.estore.core.model.OrderSummary;
import com.appsdeveloperblog.estore.ordersservice.core.data.OrderEntity;
import com.appsdeveloperblog.estore.ordersservice.core.data.OrdersRepository;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.stereotype.Component;

@Component
public class OrderQueriesHandler {

    private OrdersRepository ordersRepository;

    public OrderQueriesHandler(OrdersRepository ordersRepository){
        this.ordersRepository = ordersRepository;
    }

    @QueryHandler
    public OrderSummary findOrder(FindOrdersQuery findOrdersQuery){
        OrderEntity orderEntity = ordersRepository.findByOrderId(findOrdersQuery.getOrderId());
        return new OrderSummary(orderEntity.getOrderId(), orderEntity.getOrderStatus(),"teste");
    }


}
