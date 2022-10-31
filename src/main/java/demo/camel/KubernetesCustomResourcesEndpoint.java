package demo.camel;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.component.kubernetes.KubernetesConfiguration;

/**
 * Override endpoint provided by Camel to provide a custom consumer
 */
public class KubernetesCustomResourcesEndpoint extends org.apache.camel.component.kubernetes.customresources.KubernetesCustomResourcesEndpoint {
    public KubernetesCustomResourcesEndpoint(String uri, KubernetesCustomResourcesComponent component,
                                             KubernetesConfiguration config) {
        super(uri, component, config);
    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        Consumer consumer = new KubernetesCustomResourcesConsumer(this, processor);
        configureConsumer(consumer);
        return consumer;
    }
}
