package demo.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.spi.GeneratedPropertyConfigurer;
import org.apache.camel.spi.PropertyConfigurerGetter;
import org.apache.camel.support.component.PropertyConfigurerSupport;

public class KubernetesCustomResourcesComponentConfigurer extends PropertyConfigurerSupport implements GeneratedPropertyConfigurer, PropertyConfigurerGetter {

    @Override
    public boolean configure(CamelContext camelContext, Object obj, String name, Object value, boolean ignoreCase) {
        KubernetesCustomResourcesComponent target = (KubernetesCustomResourcesComponent) obj;
        switch (ignoreCase ? name.toLowerCase() : name) {
            case "autowiredenabled":
            case "autowiredEnabled": target.setAutowiredEnabled(property(camelContext, boolean.class, value)); return true;
            case "bridgeerrorhandler":
            case "bridgeErrorHandler": target.setBridgeErrorHandler(property(camelContext, boolean.class, value)); return true;
            case "kubernetesclient":
            case "kubernetesClient": target.setKubernetesClient(property(camelContext, io.fabric8.kubernetes.client.KubernetesClient.class, value)); return true;
            case "lazystartproducer":
            case "lazyStartProducer": target.setLazyStartProducer(property(camelContext, boolean.class, value)); return true;
            default: return false;
        }
    }

    @Override
    public String[] getAutowiredNames() {
        return new String[]{"kubernetesClient"};
    }

    @Override
    public Class<?> getOptionType(String name, boolean ignoreCase) {
        switch (ignoreCase ? name.toLowerCase() : name) {
            case "autowiredenabled":
            case "autowiredEnabled": return boolean.class;
            case "bridgeerrorhandler":
            case "bridgeErrorHandler": return boolean.class;
            case "kubernetesclient":
            case "kubernetesClient": return io.fabric8.kubernetes.client.KubernetesClient.class;
            case "lazystartproducer":
            case "lazyStartProducer": return boolean.class;
            default: return null;
        }
    }

    @Override
    public Object getOptionValue(Object obj, String name, boolean ignoreCase) {
        KubernetesCustomResourcesComponent target = (KubernetesCustomResourcesComponent) obj;
        switch (ignoreCase ? name.toLowerCase() : name) {
            case "autowiredenabled":
            case "autowiredEnabled": return target.isAutowiredEnabled();
            case "bridgeerrorhandler":
            case "bridgeErrorHandler": return target.isBridgeErrorHandler();
            case "kubernetesclient":
            case "kubernetesClient": return target.getKubernetesClient();
            case "lazystartproducer":
            case "lazyStartProducer": return target.isLazyStartProducer();
            default: return null;
        }
    }
}
