package com.example.demo

import io.saagie.argo.api.model.workflow.Workflow
import org.apache.camel.builder.RouteBuilder
import org.apache.camel.component.jackson.JacksonDataFormat
import org.apache.camel.component.kubernetes.KubernetesConstants
import org.springframework.stereotype.Component

//@Component("KubernetesRouteBuilder")
class KubernetesRouteBuilder : RouteBuilder() {
    override fun configure() {
        from("timer://kubernetesResources?repeatCount=1")
            .routeId("k8sCronWorkflowsList")
            .setHeader(KubernetesConstants.KUBERNETES_CRD_NAME, constant("useless"))
            .setHeader(KubernetesConstants.KUBERNETES_CRD_GROUP, constant("argoproj.io"))
            .setHeader(KubernetesConstants.KUBERNETES_CRD_SCOPE, constant("Cluster"))
            .setHeader(KubernetesConstants.KUBERNETES_CRD_PLURAL, constant("cronworkflows"))
            .setHeader(KubernetesConstants.KUBERNETES_CRD_VERSION, constant("v1alpha1"))
            .setHeader(KubernetesConstants.KUBERNETES_CRD_LABELS) {
                mapOf("io.saagie/installationId" to getenvOrElse("COMMON_NAMESPACE", "dev3787"))
            }
            .to("kubernetes-custom-resources:https://35.233.53.73?operation=listCustomResourcesByLabels&namespace=useless")
            .split(body())
            .to("log:error")

        from("kubernetes-custom-resources:https://35.233.53.73?namespace=&crdName=useless&crdGroup=argoproj.io&crdScope=Cluster&crdPlural=cronworkflows&crdVersion=v1alpha1&labelKey=io.saagie/installationId&labelValue=dev3787")
            .routeId("k8sArgoWatcher")
            .unmarshal(JacksonDataFormat(Workflow::class.java))
            .process {
                val message = it.getIn()
                val cm = message.getBody(Workflow::class.java)
                log.info("Got event with custom resource job id: ${cm?.metadata?.labels?.get("io.saagie/job-id")} and action ${message.getHeader(KubernetesConstants.KUBERNETES_CRD_EVENT_ACTION)}")
            }
            .to("log:warn")

        from("timer://kubernetesResources?repeatCount=1")
            .routeId("k8sDeploymentsList")
            .setHeader(KubernetesConstants.KUBERNETES_DEPLOYMENTS_LABELS) {
                mapOf("io.saagie/installationId" to "dev3787")
            }
            .to("kubernetes-deployments:https://35.233.53.73?operation=listDeploymentsByLabels")
            .split(body())
            .to("log:warn")
    }
}

fun getenvOrElse(arg: String, default: String): String {
    return System.getenv(arg) ?: default
}
