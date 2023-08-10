package com.appsdeveloperblog.estore.ordersservice.query.rest;

import com.appsdeveloperblog.estore.ordersservice.core.data.OrderEntity;
import com.appsdeveloperblog.estore.ordersservice.core.data.OrdersRepository;
import com.appsdeveloperblog.estore.ordersservice.query.FindOrdersQuery;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class OrdersQueryHandler {

    private final OrdersRepository ordersRepository;

    public OrdersQueryHandler(OrdersRepository ordersRepository) {
        this.ordersRepository = ordersRepository;
    }

    @QueryHandler
    public List<OrdersRestModel> findOrders(FindOrdersQuery query){
        List<OrdersRestModel> ordersRest = new ArrayList<>();
        List<OrderEntity> orderEntityList = ordersRepository.findAll();
        for(OrderEntity orderEntity : orderEntityList){
            OrdersRestModel ordersRestModel = new OrdersRestModel();
            BeanUtils.copyProperties(orderEntity, ordersRestModel);
            ordersRest.add(ordersRestModel);
        }
        return ordersRest;
    }
}
