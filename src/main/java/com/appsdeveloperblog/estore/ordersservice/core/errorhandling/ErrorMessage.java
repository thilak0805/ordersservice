package com.appsdeveloperblog.estore.ordersservice.core.errorhandling;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Date;

@Data
@AllArgsConstructor
public class ErrorMessage {

    private final Date timeStamp;
    private final String message;
}
