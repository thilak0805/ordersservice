package com.appsdeveloperblog.estore.ordersservice.core.events;


import com.appsdeveloperblog.estore.core.model.OrderStatus;
import lombok.Data;

@Data
public class OrderApprovedEvent {
    private final String orderId;
    private final OrderStatus orderStatus = OrderStatus.APPROVED;

}
