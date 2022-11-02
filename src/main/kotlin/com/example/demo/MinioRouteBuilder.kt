package com.example.demo

import io.minio.ListObjectsArgs
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.minio.MinioConstants
import org.springframework.stereotype.Component
import java.util.*

@Component("MinioRouteBuilder")
class MinioRouteBuilder : RouteBuilder() {
    override fun configure() {
        from("file:src/main/resources/assets?noop=true")
            .routeId("minioCommonUpload")
            .process {
                it.getIn().setHeader(MinioConstants.OBJECT_NAME, it.getIn().getHeader("CamelFileName"))
            }
            .to("log:info")
            .to("minio://external-technology-scripts")

        from("timer://minioCommon?delay=5000&period=5000")
            .routeId("minioListExternalTechnologies")
            .setBody { ListObjectsArgs.builder()
                .bucket("external-technology-scripts")
                .recursive(true) }
            .to("minio://external-technology-scripts?operation=listObjects&pojoRequest=true")
            .split(bodyAs(Iterable::class.java))
            .setBody().spel("external-technology-scripts bucket contains script: #{request.body.get().objectName}")
            .to("log:warn")
    }
}
