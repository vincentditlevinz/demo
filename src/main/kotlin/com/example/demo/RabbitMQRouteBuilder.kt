package com.example.demo

import org.apache.camel.builder.RouteBuilder
import org.springframework.stereotype.Component

@Component
class RabbitMQRouteBuilder : RouteBuilder() {
    override fun configure() {
        from("timer://myTimer?period=2000")
            .setBody()
            .spel("Hello World Camel fired at #{request.headers['firedTime']}")
            .to("log:info")
            .to("rabbitmq:amq.direct?guaranteedDeliveries=true&passive=true&queue=CPGateway")

        from("rabbitmq:amq.direct?guaranteedDeliveries=true&passive=true&queue=CPGateway")
            .to("log:warn")
    }
}
