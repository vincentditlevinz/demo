package com.example.demo

import org.springframework.amqp.core.Binding
import org.springframework.amqp.core.BindingBuilder
import org.springframework.amqp.core.DirectExchange
import org.springframework.amqp.core.Queue
import org.springframework.amqp.rabbit.connection.ConnectionFactory
import org.springframework.amqp.rabbit.core.RabbitAdmin
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration


@Configuration
class RabbitMQAdminConfiguration {
    @Bean
    fun admin(factory: ConnectionFactory): RabbitAdmin {
        return RabbitAdmin(factory)
    }

    @Bean
    fun controlPlaneGateway(): DirectExchange {
        return DirectExchange("CPGateway", true, false)
    }

    @Bean
    fun controlPlaneGatewayQueue(): Queue {
        return Queue("CPGatewayQueue", true, false, false)
    }

    @Bean
    fun controlPlaneBinding(): Binding {
        return BindingBuilder.bind(controlPlaneGatewayQueue()).to(controlPlaneGateway()).with("CPGatewayOrders")
    }
}
