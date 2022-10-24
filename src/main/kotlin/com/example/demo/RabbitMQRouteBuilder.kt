package com.example.demo

import org.apache.camel.builder.RouteBuilder
import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class RabbitMQRouteBuilder : RouteBuilder() {

    override fun configure() {
        from("timer://myTimer?period=2000")
            .setBody()
            .spel("Hello World Camel fired at #{request.headers['firedTime']}")
            .to("log:info")
            .to("spring-rabbitmq:CPGateway?routingKey=CPGatewayOrders")

        from("spring-rabbitmq:CPGateway?queues=CPGatewayQueue")
            .process {
                if(RabbitMQRouteObject.random.nextInt() % 2 == 0)
                    throw Exception("Random failure")
                else
                    log.info("Processing message successfully")
            }
            .to("log:warn")
    }
}

object RabbitMQRouteObject {
    internal val random = Random(0)
}
