package com.appsdeveloperblog.estore.ordersservice.command.interceptor;

import com.appsdeveloperblog.estore.ordersservice.command.CreateOrderCommand;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.messaging.MessageDispatchInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.BiFunction;

@Component
public class CreateOrderCommandInterceptor implements MessageDispatchInterceptor<CommandMessage<?>> {

    Logger logger = LoggerFactory.getLogger(CreateOrderCommandInterceptor.class);

    @Override
    public BiFunction<Integer, CommandMessage<?>, CommandMessage<?>> handle(List<? extends CommandMessage<?>> list) {
        return (index, command)->{
            logger.info("intercepted command====={}",command.getCommandName());
            logger.info("payload type============{}",command.getPayloadType());
            if(CreateOrderCommand.class.equals(command.getPayloadType())){
                CreateOrderCommand createOrderCommand = (CreateOrderCommand) command.getPayload();
                if(createOrderCommand.getQuantity()<=0){
                    throw new IllegalArgumentException("quantity cannot be less than or equal to zero");
                }
                if(createOrderCommand.getAddressId()==null || createOrderCommand.getAddressId().isBlank()){
                    throw new IllegalArgumentException("address id cannot be empty");
                }
            }
            return command;
        };
    }
}
