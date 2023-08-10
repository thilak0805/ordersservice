package com.appsdeveloperblog.estore.ordersservice;

import com.appsdeveloperblog.estore.ordersservice.command.interceptor.CreateOrderCommandInterceptor;
import org.axonframework.commandhandling.CommandBus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class OrdersserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrdersserviceApplication.class, args);
	}

	@Autowired
	public void registerCreateOrderCommandInterceptor(ApplicationContext contxt, CommandBus commandBus){
		commandBus.registerDispatchInterceptor(contxt.getBean(CreateOrderCommandInterceptor.class));
	}

}
