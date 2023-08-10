package com.appsdeveloperblog.estore.ordersservice.query.rest;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
public class OrdersRestModel {

    @NotBlank(message ="productid is required field")
    private String productId;
    @Min(value=1, message = "quantity cannot be lower than 1")
    @Max(value=5, message = "quanitity cannot be longer than 5")
    private int quantity;
    private String addressId;
}
