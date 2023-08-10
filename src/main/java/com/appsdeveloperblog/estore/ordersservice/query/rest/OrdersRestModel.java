package com.appsdeveloperblog.estore.ordersservice.query.rest;

import lombok.Data;

@Data
public class OrdersRestModel {

    private String productId;
    private int quantity;
    private String addressId;
}
