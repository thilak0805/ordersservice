package com.appsdeveloperblog.estore.ordersservice.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

@Data
@AllArgsConstructor
public class ApproveOrderCommand {
    @TargetAggregateIdentifier
    private String orderId;
}
