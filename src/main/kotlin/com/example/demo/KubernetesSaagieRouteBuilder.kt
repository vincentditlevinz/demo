package com.example.demo

import demo.camel.KubernetesCustomResourcesComponent
import demo.camel.KubernetesCustomResourcesEndpointUriFactory
import io.saagie.argo.api.model.workflow.Workflow
import org.apache.camel.ExtendedCamelContext
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.kubernetes.KubernetesConstants
import org.springframework.stereotype.Component

@Component("KubernetesSaagieRouteBuilder")
class KubernetesSaagieRouteBuilder : RouteBuilder() {

    override fun configure() {
        registerCustomComponent()
        from("saagie-kubernetes-custom-resources:https://35.233.53.73?namespace=&resourceName=workflow")
            .routeId("k8sArgoWatcher")
            .process {
                val message = it.getIn()
                val cm = message.getBody(Workflow::class.java)
                log.info("Got event with custom resource job id: ${cm?.metadata?.labels?.get("io.saagie/job-id")} and action ${message.getHeader(KubernetesConstants.KUBERNETES_CRD_EVENT_ACTION)}")
            }
            .to("log:warn")

        from("timer://kubernetesResources?repeatCount=1")
            .routeId("k8sArgoAnalysis")
            .setHeader(KubernetesConstants.KUBERNETES_CRD_NAME, constant("useless"))
            .setHeader(KubernetesConstants.KUBERNETES_CRD_GROUP, constant("argoproj.io"))
            .setHeader(KubernetesConstants.KUBERNETES_CRD_SCOPE, constant("Cluster"))
            .setHeader(KubernetesConstants.KUBERNETES_CRD_PLURAL, constant("cronworkflows"))
            .setHeader(KubernetesConstants.KUBERNETES_CRD_VERSION, constant("v1alpha1"))
            .setHeader(KubernetesConstants.KUBERNETES_CRD_LABELS) {
                mapOf("io.saagie/installationId" to getenvOrElse("COMMON_NAMESPACE", "dev3787"))
            }
            .to("saagie-kubernetes-custom-resources:https://35.233.53.73?operation=listCustomResourcesByLabels&namespace=useless")
            .split(body())
            .to("log:error")
    }

    /**
     * Camel spi mechanism force to package the custom component in its own jar. Here we explicitly load in-project custom component code.
     */
    private fun registerCustomComponent() {
        camelContext.addComponent("saagie-kubernetes-custom-resources", KubernetesCustomResourcesComponent())
        camelContext.registry.bind("saagie-kubernetes-custom-resources", KubernetesCustomResourcesEndpointUriFactory())
        log.info(
            camelContext.adapt(ExtendedCamelContext::class.java)
                .getEndpointUriFactory("saagie-kubernetes-custom-resources").propertyNames().toString()
        )
    }
}
