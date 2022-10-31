package demo.camel;

import io.fabric8.kubernetes.api.model.KubernetesResourceList;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.client.Watch;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.WatcherException;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.saagie.argo.api.model.workflow.Workflow;
import io.saagie.argo.api.model.workflow.WorkflowList;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.kubernetes.AbstractKubernetesEndpoint;
import org.apache.camel.component.kubernetes.KubernetesConstants;
import org.apache.camel.component.kubernetes.KubernetesHelper;
import org.apache.camel.support.DefaultConsumer;
import org.apache.camel.util.ObjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;

/**
 * Override consumer provided by Camel to use model (like argo Workflow class) instead of generic resource (easier to configure for our use case)
 */
public class KubernetesCustomResourcesConsumer extends DefaultConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(KubernetesCustomResourcesConsumer.class);

    private final Processor processor;
    private ExecutorService executor;
    private CustomResourcesConsumerTask customResourcesWatcher;

    public KubernetesCustomResourcesConsumer(AbstractKubernetesEndpoint endpoint, Processor processor) {
        super(endpoint, processor);
        this.processor = processor;
    }

    @Override
    public AbstractKubernetesEndpoint getEndpoint() {
        return (AbstractKubernetesEndpoint) super.getEndpoint();
    }

    @Override
    protected void doStart() throws Exception {
        super.doStart();
        executor = getEndpoint().createExecutor();

        customResourcesWatcher = new CustomResourcesConsumerTask();
        executor.submit(customResourcesWatcher);
    }

    @Override
    protected void doStop() throws Exception {
        LOG.debug("Stopping Kubernetes Custom Resources Consumer");
        if (executor != null) {
            KubernetesHelper.close(customResourcesWatcher, customResourcesWatcher::getWatch);

            if (getEndpoint() != null && getEndpoint().getCamelContext() != null) {
                getEndpoint().getCamelContext().getExecutorServiceManager().shutdownNow(executor);
            } else {
                executor.shutdownNow();
            }
        }
        executor = null;
        super.doStop();
    }

    class CustomResourcesConsumerTask implements Runnable {

        private Watch watch;

        @Override
        public void run() {
            if (ObjectHelper.isNotEmpty(getEndpoint().getKubernetesConfiguration().getNamespace())) {
                LOG.error("namespace is not specified.");
            }
            String namespace = getEndpoint().getKubernetesConfiguration().getNamespace();
            try {
                getResources(getEndpoint().getKubernetesConfiguration().getResourceName())
                        .inNamespace(namespace)
                        .watch(new Watcher() {
                            @Override
                            public void eventReceived(Action action, Object resource) {
                                Exchange exchange = createExchange(false);
                                exchange.getIn().setBody(resource);
                                exchange.getIn().setHeader(KubernetesConstants.KUBERNETES_CRD_EVENT_ACTION, action);
                                exchange.getIn().setHeader(KubernetesConstants.KUBERNETES_CRD_EVENT_TIMESTAMP,
                                        System.currentTimeMillis());
                                try {
                                    processor.process(exchange);
                                } catch (Exception e) {
                                    getExceptionHandler().handleException("Error during processing", exchange, e);
                                } finally {
                                    releaseExchange(exchange, false);
                                }
                            }

                            @Override
                            public void onClose(WatcherException cause) {
                                if (cause != null) {
                                    LOG.error(cause.getMessage(), cause);
                                }
                            }
                        });
            } catch (Exception e) {
                LOG.error("Exception in handling githubsource instance change", e);
            }
        }

        private MixedOperation<? extends CustomResource, ? extends KubernetesResourceList<? extends CustomResource>, ? extends Resource<? extends CustomResource>> getResources(String name) {
            switch (name) {
                case "workflow":
                    return getEndpoint().getKubernetesClient().resources(Workflow.class, WorkflowList.class);
                default:
                    throw new IllegalArgumentException(String.format("Unsupported resource type: %s", name));
            }
        }

        public Watch getWatch() {
            return watch;
        }

        public void setWatch(Watch watch) {
            this.watch = watch;
        }
    }
}
