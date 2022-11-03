package com.example.demo

import com.google.protobuf.util.Timestamps
import org.apache.camel.Body
import org.apache.camel.Header
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.minio.MinioConstants
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*

@Component("GrpcRouteBuilder")
class GrpcRouteBuilder : RouteBuilder() {
    fun buildResponse(@Body request: SynchronizationServiceRequest): SynchronizationServiceResponse {
        val yaml = "I am yaml"
        val order = SyncOrder.newBuilder()
            .setId(10000L)
            .setType(SyncOrder.SyncOrderType.JobCreatedSyncOrder)
            .setCreatedAt(Timestamps.fromMillis(Instant.now().toEpochMilli()))
            .setDeployment(
                SyncOrder.Deployment.newBuilder()
                    .setLabel("PythonJob")
                    .setYaml(yaml)
            )
            .build()
        return SynchronizationServiceResponse.newBuilder().addOrders(order).build()
    }

    override fun configure() {
        from("grpc://0.0.0.0:9991/com.example.demo.SynchronizationService?consumerStrategy=PROPAGATION")
            .to("log://before?showProperties=true&showHeaders=true")
            .bean("GrpcRouteBuilder", "buildResponse")
    }
}
