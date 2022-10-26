package com.example.demo

import com.google.protobuf.util.Timestamps
import org.apache.camel.builder.RouteBuilder
import org.springframework.stereotype.Component
import java.util.*

@Component
class RabbitMQRouteBuilder : RouteBuilder() {
    internal fun buildMessage(date: Date): SyncOrder {
        val yaml = "I am yaml"
        return SyncOrder.newBuilder()
            .setId(10000L)
            .setType(SyncOrder.SyncOrderType.JobCreatedSyncOrder)
            .setCreatedAt(Timestamps.fromMillis(date.toInstant().toEpochMilli()))
            .setDeployment(
                SyncOrder.Deployment.newBuilder()
                    .setLabel("PythonJob")
                    .setYaml(yaml)
            )
            .build()
    }

    override fun configure() {
        from("timer://myTimer?period=2000")
            .process {
                val date = it.getIn().getHeader("firedTime", Date::class.java)
                val message = buildMessage(date)
                it.getIn().setBody(message.toByteArray(), ByteArray::class.java)
            }
            .to("log:info")
            .to("spring-rabbitmq:CPGateway?routingKey=CPGatewayOrders")

        from("spring-rabbitmq:CPGateway?queues=CPGatewayQueue")
            .process {
                val messageAsBytes = it.getIn().getBody(ByteArray::class.java)
                it.getIn().setBody(SyncOrder.parseFrom(messageAsBytes), String::class.java)
            }
            .to("log:warn")
    }
}
