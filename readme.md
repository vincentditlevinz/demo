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
* spring.rabbitmq.username=the username as printed during [Use Management console](#how-to-use-management-console)
* spring.rabbitmq.password=the password as printed during [Use Management console](#how-to-use-management-console)
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
The application run several routes:
1. k8sCronWorkflowsList: list K8s CronWorkflows custom resource using default Camel component 
2. k8sArgoWatcher: watch K8s CronWorkflow custom resource using default Camel component 
3. k8sDeploymentsList: list K8s deployments using default Camel component 
4. saagie-k8sCronWorkflowsList: list K8s CronWorkflows custom resource using a custom component derived from Camel one
5. saagie-kk8sArgoWatcher: watch K8s CronWorkflow custom resource using a custom component derived from Camel one 
6. minioCommonUpload: upload a file in minio common 
7. minioListExternalTechnologies: list minio objects in external technologies bucket 
8. writeSyncOrders: send a protobuf message in a dedicated RabbitMQ queue 
9. readSyncOrders: read a protobuf message from a dedicated RabbitMQ queue 
10. downloadApi(including serveFromMinio): Rest api to download a file, use `http://localhost:8080/api/download/module.js` for testing.

For the moment this application is just a simple POC that is not configured to be run in K8s but just on your laptop. To enable K8s connection you should run:

```
kubectl port-forward "service/hello-world" 5672
kubectl port-forward "service/saagie-common-minio" 9000
```

# Proof of work
## K8s watcher
![K8s watcher outputs](./img/Capture%20d’écran%20du%202022-11-02%2012-28-07.png)

## A note on large files
* We tested a [1 GB file](https://testfiledownload.com/) upload successfully with `minioCommonUpload` route
* We tested a 1 GB file download successfully with `downloadApi` route.

# Advantages of using Apache Camel over handmade code
Obviously one can always write all by hand but, here are a few arguments in favor of using Apache Camel:
* Code handled by Apache Camel IS NOT business code but low level technical integration code. As a rule of thumb, we write software for our core business and buy software for the rest. The same should apply when considering business and technical code !
* A homogeneous DSL to integrate everything
* A lot of out of the box connectors (as shown in this POC overriding a built-in component is rather easy)
* A lot is done behind the scene, for instance error handling, retry, monitoring...
* Apache Camel [monitoring metrics can be sent to tools like Prometheus](https://danielblancocuadrado.medium.com/apache-camel-create-your-own-metric-with-micrometer-b10d2db09b4f)
* Apache Camel supports [health checks](https://camel.apache.org/manual/health-check.html) which is very useful in a K8s environment

One might argue that learning Apache Camel is hard:
* A lot of documentations and books do exist
* Apache Camel has also [commercial support offering](https://camel.apache.org/manual/commercial-camel-offerings.html) for training support
* Apache Camel, like any technology as Springframework, Java..., is a competence one can find in the developers' market
* The competence of writing very low level technical stuffs the right way might be harder to find
