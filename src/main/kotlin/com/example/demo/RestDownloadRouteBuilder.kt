package com.example.demo

import org.apache.camel.builder.RouteBuilder
import org.springframework.stereotype.Component

@Component("RestDownloadRouteBuilder")
class RestDownloadRouteBuilder : RouteBuilder() {

    override fun configure() {
        restConfiguration().component("servlet")
            .host("localhost").port(8080)

        rest("/download")
            .get()
            .routeId("downloadApi")
            .produces(org.springframework.http.MediaType.APPLICATION_OCTET_STREAM_VALUE)
            .to("direct:download")

        from("direct:download")
            .routeId("serveFiles")
            .pollEnrich("file:src/main/resources/assetsForDownload?noop=true")
            .setHeader("Content-Disposition").spel("attachment;filename=#{headers.CamelFileName}")
    }
}
