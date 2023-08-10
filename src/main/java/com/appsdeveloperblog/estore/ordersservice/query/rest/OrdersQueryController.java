package com.appsdeveloperblog.estore.ordersservice.query.rest;

import com.appsdeveloperblog.estore.ordersservice.query.FindOrdersQuery;
import com.netflix.discovery.converters.Auto;
import org.axonframework.commandhandling.gateway.CommandGateway;
import org.axonframework.messaging.responsetypes.ResponseTypes;
import org.axonframework.queryhandling.QueryGateway;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrdersQueryController {

    @Autowired
    QueryGateway queryGateway;

    public List<OrdersRestModel> getOrders(){
        FindOrdersQuery findOrdersQuery = new FindOrdersQuery();
        //command gateway sends the command obj to the command bus, command bus routes the command obj to command handler
        // the below query accepts a query that we want to send it to query bus and accepts the return type that will be returned
      List<OrdersRestModel> orders =   queryGateway.query(findOrdersQuery, ResponseTypes.multipleInstancesOf(OrdersRestModel.class)).join();
      return orders;
    }
}
