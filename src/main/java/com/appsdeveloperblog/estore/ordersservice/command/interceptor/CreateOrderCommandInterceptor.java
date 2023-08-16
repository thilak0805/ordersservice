package com.appsdeveloperblog.estore.ordersservice.command.interceptor;

import com.appsdeveloperblog.estore.ordersservice.command.CreateOrderCommand;
import com.appsdeveloperblog.estore.ordersservice.core.data.OrderLookupEntity;
import com.appsdeveloperblog.estore.ordersservice.core.data.OrderLookupRepository;
import org.axonframework.commandhandling.CommandMessage;
import org.axonframework.messaging.MessageDispatchInterceptor;
import org.hibernate.criterion.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.BiFunction;

@Component
public class CreateOrderCommandInterceptor implements MessageDispatchInterceptor<CommandMessage<?>> {

    Logger logger = LoggerFactory.getLogger(CreateOrderCommandInterceptor.class);

    private final OrderLookupRepository orderLookupRepository;

    public CreateOrderCommandInterceptor(OrderLookupRepository orderLookupRepository){
        this.orderLookupRepository = orderLookupRepository;
    }

    @Override
    public BiFunction<Integer, CommandMessage<?>, CommandMessage<?>> handle(List<? extends CommandMessage<?>> list) {
        return (index, command)->{
            logger.info("intercepted command====={}",command.getCommandName());
            logger.info("payload type============{}",command.getPayloadType());
            if(CreateOrderCommand.class.equals(command.getPayloadType())){
                CreateOrderCommand createOrderCommand = (CreateOrderCommand) command.getPayload();
              /*  if(createOrderCommand.getProductId()==null || createOrderCommand.getProductId().isBlank() || createOrderCommand.getProductId().isEmpty()){
                    throw new IllegalArgumentException("product Id is empty");
                }
                if(createOrderCommand.getQuantity()<=0){
                    throw new IllegalArgumentException("quantity cannot be less than or equal to zero");
                }
                if(createOrderCommand.getAddressId()==null || createOrderCommand.getAddressId().isBlank()){
                    throw new IllegalArgumentException("address id cannot be empty");
                }*/
            //query to check if order already present
                OrderLookupEntity orderLookupEntity
                        = orderLookupRepository.findByProductIdOrAddressId(createOrderCommand.getProductId(),createOrderCommand.getAddressId());
                logger.info("orderLookupEntity============{}",orderLookupEntity);
                if(orderLookupEntity!=null){
                    throw new IllegalArgumentException(String.format("Order with Order id %s or addressId already exists",
                            createOrderCommand.getProductId(), createOrderCommand.getAddressId()));
                }
            }
            return command;
        };
    }
}
