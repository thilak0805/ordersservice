package com.appsdeveloperblog.estore.ordersservice.command.rest;

import lombok.Data;

@Data
public class CreateOrdersRestModel {

    private String productId;
    private int quantity;
    private String addressId;
}
