package com.appsdeveloperblog.estore.ordersservice.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FindOrdersQuery {
    private  String orderId;
}
