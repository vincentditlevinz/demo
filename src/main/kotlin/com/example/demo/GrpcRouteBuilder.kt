package com.example.demo

import com.google.protobuf.util.Timestamps
import io.grpc.Status
import org.apache.camel.Body
import org.apache.camel.builder.RouteBuilder
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*

@Component("GrpcRouteBuilder")
class GrpcRouteBuilder : RouteBuilder() {
    fun buildResponse(@Body request: GetSyncOrdersRequest): GetSyncOrdersResponse {
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
        return GetSyncOrdersResponse.newBuilder().addOrders(order).build()
    }

    fun buildError() {
        throw Status.UNIMPLEMENTED.withDescription("Unimplemented end-point").asRuntimeException()
    }

    override fun configure() {
        from("grpc://0.0.0.0:9991/com.example.demo.SynchronizationService?consumerStrategy=PROPAGATION")
            .routeId("grpcServiceRouter")
            .choice()
            .`when`(header("CamelGrpcMethodName").isEqualTo("getSyncOrders"))// Note the `when` because when is a reserved keyword in Kotlin
                .bean("GrpcRouteBuilder", "buildResponse")
            .otherwise()
                .bean("GrpcRouteBuilder", "buildError")
    }
}
