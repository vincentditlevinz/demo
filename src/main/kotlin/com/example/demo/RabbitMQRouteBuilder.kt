package com.example.demo

import com.google.protobuf.util.Timestamps
import org.apache.camel.Body
import org.apache.camel.Header
import org.apache.camel.builder.RouteBuilder
import org.springframework.stereotype.Component
import java.util.*

@Component("RabbitMQRouteBuilder")
class RabbitMQRouteBuilder : RouteBuilder() {
    fun buildSyncOrder(@Header("firedTime") date: Date): ByteArray {
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
            .build().toByteArray()
    }

    fun extractSyncOrder(@Body message: ByteArray): SyncOrder {
        return SyncOrder.parseFrom(message)
    }

    override fun configure() {
        from("timer://pollSyncOrdersFromControlPlane?period=2000")
            .routeId("writeSyncOrders")
            .bean("RabbitMQRouteBuilder", "buildSyncOrder")
            .to("log:info")
            .to("spring-rabbitmq:CPGateway?routingKey=CPGatewayOrders")

        from("spring-rabbitmq:CPGateway?queues=CPGatewayQueue")
            .routeId("readSyncOrders")
            .bean("RabbitMQRouteBuilder", "extractSyncOrder")
            .to("log:warn")
    }
}
