package com.example.demo

import io.minio.ListObjectsArgs
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.minio.MinioConstants
import org.springframework.stereotype.Component

@Component("RestDownloadRouteBuilder")
class RestDownloadRouteBuilder : RouteBuilder() {

    override fun configure() {
        restConfiguration().component("servlet")
            .host("localhost").port(8080)

        rest("/download/{path}")
            .get()
            .routeId("downloadApi")
            .produces(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE)
            .to("direct:downloadMinio")

        from("direct:downloadMinio")
            .routeId("serveFromMinio")
            .setBody { ListObjectsArgs.builder()
                .bucket("external-technology-scripts")
                .recursive(true) }
            .process {
                it.getIn().setHeader(MinioConstants.OBJECT_NAME, it.getIn().getHeader("path"))
            }
            .pollEnrich("minio://external-technology-scripts?operation=getObject&pojoRequest=true", 10000)
            .setHeader("Content-Disposition").spel("attachment;filename=#{headers.CamelMinioObjectName}")
    }
}
