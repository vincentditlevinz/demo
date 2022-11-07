package com.example.demo

import com.google.protobuf.util.Timestamps
import io.grpc.Status
import io.grpc.StatusRuntimeException
import org.apache.camel.Body
import org.apache.camel.ExchangeProperties
import org.apache.camel.Header
import org.apache.camel.builder.RouteBuilder
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.*
import java.util.Map

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


    /* Static router cannot be used with Kotlin because of 'when' reserved keyword, we must use a dynamic router which is a bit more involved */
    fun routing(@Header("CamelGrpcMethodName") grpcMethodName: String, @ExchangeProperties properties: Map<String, String>): String? {
        val route = properties.getOrDefault("route", run {
            if (grpcMethodName == "getSyncOrders") {
                "direct:getSyncOrders"
            } else {
                "direct:error"
            }
        })
        properties.put("route", "end")
        return if(route == "end")
            null
        else
            route
    }

    override fun configure() {
        from("grpc://0.0.0.0:9991/com.example.demo.SynchronizationService?consumerStrategy=PROPAGATION")
            .routeId("grpcServiceRouter")
            .dynamicRouter(method(GrpcRouteBuilder::class.java, "routing"))

        from("direct:getSyncOrders")
            .routeId("getSyncOrders")
            .bean("GrpcRouteBuilder", "buildResponse")

        from("direct:error")
            .routeId("grpcError")
            .bean("GrpcRouteBuilder", "buildError")
    }
}
