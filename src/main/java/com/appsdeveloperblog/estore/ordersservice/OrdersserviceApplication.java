package com.appsdeveloperblog.estore.ordersservice;

import com.appsdeveloperblog.estore.ordersservice.command.interceptor.CreateOrderCommandInterceptor;
import com.appsdeveloperblog.estore.ordersservice.core.errorhandling.OrderServiceEventsErrorHandler;
import org.axonframework.commandhandling.CommandBus;
import org.axonframework.config.Configuration;
import org.axonframework.config.ConfigurationScopeAwareProvider;
import org.axonframework.config.EventProcessingConfigurer;
import org.axonframework.deadline.DeadlineManager;
import org.axonframework.deadline.SimpleDeadlineManager;
import org.axonframework.spring.messaging.unitofwork.SpringTransactionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
public class OrdersserviceApplication {

	public static void main(String[] args) {
		SpringApplication.run(OrdersserviceApplication.class, args);
	}

	//@Autowired
	public void registerCreateOrderCommandInterceptor(ApplicationContext contxt, CommandBus commandBus){
		commandBus.registerDispatchInterceptor(contxt.getBean(CreateOrderCommandInterceptor.class));
	}

	@Autowired
	public void configure(EventProcessingConfigurer config){
		config.registerListenerInvocationErrorHandler("order-group", conf-> new OrderServiceEventsErrorHandler());

	}

	@Bean
	public DeadlineManager deadlineManager(Configuration configuration, SpringTransactionManager transactionManager){
		return SimpleDeadlineManager.builder()
				.scopeAwareProvider(new ConfigurationScopeAwareProvider(configuration))
				.transactionManager(transactionManager)
				.build();
	}

}
