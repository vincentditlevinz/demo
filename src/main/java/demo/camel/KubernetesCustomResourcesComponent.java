package demo.camel;

import org.apache.camel.component.kubernetes.KubernetesConfiguration;
import org.apache.camel.component.kubernetes.customresources.KubernetesCustomResourcesComponentConfigurer;
import org.apache.camel.component.kubernetes.customresources.KubernetesCustomResourcesEndpointConfigurer;
import org.apache.camel.spi.PropertyConfigurer;

/**
 * Override component provided by Camel to provide our custom endpoint and force usage of Camel configurers
 */
public class KubernetesCustomResourcesComponent extends org.apache.camel.component.kubernetes.customresources.KubernetesCustomResourcesComponent {
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
