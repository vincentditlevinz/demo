# SpringBoot + Camel + RabbitMQ
This demo shows how to use Apache Camel in a SpringBoot application to read/write into an already existing queue.
## RabbitMQ setup
We use a RabbitMQ setup in a K8s GCP cluster. The setup is same as the one described in [RabbitMQ official documentation](https://www.rabbitmq.com/kubernetes/operator/quickstart-operator.html)

### How to use Management console

```
username="$(kubectl get secret hello-world-default-user -o jsonpath='{.data.username}' | base64 --decode)"
echo "username: $username"
password="$(kubectl get secret hello-world-default-user -o jsonpath='{.data.password}' | base64 --decode)"
echo "password: $password"

kubectl port-forward "service/hello-world" 15672
```

Open your [browser](http://localhost:15672/) 
You do not need to create all the AMQP boilerplate (exchanges, queues, bindings), this is handled by [RabbitMQAdminConfiguration](https://github.com/vincentditlevinz/demo/blob/e8c7208841beede9ac0214070edb1a1cd1cca66c/src/main/kotlin/com/example/demo/RabbitMQAdminConfiguration.kt#L14).

# Camel application

As we need transactional consumer pattern, we ended with the [Spring-RabbitMQ](https://camel.apache.org/components/latest/spring-rabbitmq-component.html) Camel component instead of the [RabbitMQ](https://camel.apache.org/components/latest/rabbitmq-component.html) component.
## Camel application setup
* spring.rabbitmq.username=the username as printed during [Use Management console](#use-management-console)
* spring.rabbitmq.password=the password as printed during [Use Management console](#use-management-console)
* spring.rabbitmq.host=localhost
* spring.rabbitmq.port=5672
* camel.component.minio.access-key=the username as printed during [Minio credentials](#minio-credentials)
* camel.component.minio.secret-key=the password as printed during [Minio credentials](#minio-credentials)

## Minio credentials

```
username="$(kubectl get secret saagie-common-minio-root -o jsonpath='{.data.rootUser}' | base64 -d)"
password="$(kubectl get secret saagie-common-minio-root -o jsonpath='{.data.rootPassword}' | base64 -d)"
```

## Running Camel application
The application run two simple routes:
* A first route builds a message on a regular basis (timer) and sends it to RabbitMQ CPGateway queue
* A second route reads from RabbitMQ CPGateway queue and print the message at WARN level

For the moment this application is just a simple POC that is not configured to be run in K8s but just on your laptop. To enable K8s connection you should run:

```
kubectl port-forward "service/hello-world" 5672
kubectl port-forward "service/saagie-common-minio" 9000
```

