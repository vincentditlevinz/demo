package com.example.demo

import com.google.protobuf.util.Timestamps
import io.minio.ListObjectsArgs
import org.apache.camel.Body
import org.apache.camel.Header
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.minio.MinioConstants
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

        from("file:src/main/resources/assets?noop=true")
            .routeId("minioCommonUpload")
            .process {
                it.getIn().setHeader(MinioConstants.OBJECT_NAME, it.getIn().getHeader("CamelFileName"))
            }
            .to("log:info")
            .to("minio://external-technology-scripts")

        from("timer://minioCommon?delay=5000&period=5000")
            .routeId("minioCommon")
            .setBody { ListObjectsArgs.builder()
                .bucket("external-technology-scripts")
                .recursive(true) }
            .to("minio://external-technology-scripts?operation=listObjects&pojoRequest=true")
            .split(bodyAs(Iterable::class.java))
            .setBody().spel("external-technology-scripts bucket contains script: #{request.body.get().objectName}")
            .to("log:warn");
    }
}
