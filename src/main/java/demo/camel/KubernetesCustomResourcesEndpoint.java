package demo.camel;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.component.kubernetes.AbstractKubernetesEndpoint;
import org.apache.camel.component.kubernetes.KubernetesConfiguration;

/**
 * KubernetesCustomResources component
 */
public class KubernetesCustomResourcesEndpoint extends AbstractKubernetesEndpoint {
    public KubernetesCustomResourcesEndpoint(String uri, KubernetesCustomResourcesComponent component,
                                             KubernetesConfiguration config) {
        super(uri, component, config);
    }

    @Override
    public Producer createProducer() throws Exception {
        return new KubernetesCustomResourcesProducer(this);
    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        Consumer consumer = new KubernetesCustomResourcesConsumer(this, processor);
        configureConsumer(consumer);
        return consumer;
    }
}
