package com.example.demo

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
            .process {
                it.getIn().setHeader(MinioConstants.OBJECT_NAME, it.getIn().getHeader("path"))
            }
            .enrich("minio://external-technology-scripts?operation=getObject")
            .setHeader("Content-Disposition").spel("attachment;filename=#{headers.CamelMinioObjectName}")
    }
}
