package com.example.demo

import demo.camel.KubernetesCustomResourcesComponent
import demo.camel.KubernetesCustomResourcesEndpointUriFactory
import io.saagie.argo.api.model.workflow.Workflow
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.kubernetes.KubernetesConstants
import org.springframework.stereotype.Component

@Component("KubernetesSaagieRouteBuilder")
class KubernetesSaagieRouteBuilder : RouteBuilder() {

    override fun configure() {
        camelContext.addComponent("saagie-kubernetes-custom-resources", KubernetesCustomResourcesComponent())
        camelContext.registry.bind("saagie-kubernetes-custom-resources-endpoint", KubernetesCustomResourcesEndpointUriFactory())
        from("saagie-kubernetes-custom-resources:https://35.233.53.73?namespace=&resourceName=workflow")
            .routeId("k8sArgoWatcher")
            .process {
                val message = it.getIn()
                val cm = message.getBody(Workflow::class.java)
                log.info("Got event with custom resource job id: ${cm?.metadata?.labels?.get("io.saagie/job-id")} and action ${message.getHeader(KubernetesConstants.KUBERNETES_CRD_EVENT_ACTION)}")
            }
            .to("log:warn")
    }
}
