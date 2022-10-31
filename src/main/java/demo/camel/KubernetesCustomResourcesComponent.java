package demo.camel;

import org.apache.camel.Endpoint;
import org.apache.camel.component.kubernetes.AbstractKubernetesComponent;
import org.apache.camel.component.kubernetes.KubernetesConfiguration;
import org.apache.camel.spi.PropertyConfigurer;

import java.util.Map;

@org.apache.camel.spi.annotations.Component("saagie-kubernetes-custom-resources")
public class KubernetesCustomResourcesComponent extends AbstractKubernetesComponent {

    public KubernetesCustomResourcesComponent() {
    }

    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        return super.createEndpoint(uri, remaining, parameters);
    }

    @Override
    public PropertyConfigurer getEndpointPropertyConfigurer() {
        return new KubernetesCustomResourcesEndpointConfigurer();
    }

    @Override
    public PropertyConfigurer getComponentPropertyConfigurer() {
        return new KubernetesCustomResourcesComponentConfigurer();
    }

    protected KubernetesCustomResourcesEndpoint doCreateEndpoint(String uri, String remaining, KubernetesConfiguration config) {
        return new KubernetesCustomResourcesEndpoint(uri, this, config);
    }
}
