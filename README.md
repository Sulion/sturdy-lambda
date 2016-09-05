# sturdy-lambda

## About 

This project is a trivial map-reduce over Spark cluster powered by Kubernetes.

## Installation

### Spark Application

The files `pom.xml` and `src` directory comprise a full-fledged Java project for Apache Spark. You can build it with `mvn clean install`. In
that case `target/sturdy-lambda.jar` is a full jar you want to use. If you have already deployed Spark cluster, you can run the program with 

    ./spark-submit --name "sturdy-lambda" --class ru.rykov.rdd.App --master spark://spark-master:7077 sturdy-lambda.jar \
    <URI of data file> <URI of map script> <URI of reduce script>

where `<URI of data file>` is a fully-qualified absolute URI to your datafile, `<URI of map script>` and `<URI of reduce script>` are
Groovy-scripts, namely --- functions `int map(a,b)` and `int reduce(a,b)`. Please, comply with the naming, it's important for future invocation.

In the local context the application migth executed thus:

    ./spark-submit --name "sturdy-lambda" --class ru.rykov.rdd.App --master local[4] sturdy-lambda.jar \
    file:///mnt/data/data.txt file:///mnt/data/map.groovy file:///mnt/data/reduce.groovy

### Local Kubernetes

Kuberenetes team recently released a neat little tool called [Minikube](https://github.com/kubernetes/minikube). You may follow recommendations
on the official site or you can use my Ansible-role to install it. (Or you may try to install Kubernetes directly onto your PC or whatever
hardware you care to use)

    ansible-galaxy install Sulion.minikube_role
	ansible-playbook install/kubernetes-playbook.yml
	
That is, hopefully, it. I needed to do some small tricks afterwards: 

    my-localhost$ minikube ssh
	#That's the directory spark-nodes will expect to see as a shared one:
	minikubeVM$ sudo mkdir -p /data/spark/data

Then I copied my pub-ssh key to be able to copy the data and groovy scripts inside the VM.

### Kubernetes Spark Services

Current `sturdy-lambda` K8s-Spark configuration supports only `hostPath` share directory, which is situated at `/data/spark/data`. It's there you're
supposed to place all the data and groovy scripts. To install this configuration, ensure you have `/data/spark/data` created, then perform: 

    kubectl create -f k8s-config/spark-namespace.yaml 
	kubectl create -f k8s-config/
	
You'll see (among other things) an error message that the `spark-cluster` already exists. That's alright. At the end of the process you'll see
the following:
    
    # kubectl get all --namespace=spark-cluster
	NAME                            DESIRED      CURRENT       AGE
	spark-master-controller         1            1             31s
	spark-worker-controller         2            2             31s
	NAME                            CLUSTER-IP   EXTERNAL-IP   PORT(S)    AGE
	spark-master                    10.0.0.142   <nodes>       7077/TCP   31s
	spark-webui                     10.0.0.18    <nodes>       8080/TCP   31s
	NAME                            READY        STATUS        RESTARTS   AGE
	spark-master-controller-v38zh   1/1          Running       0          31s
	spark-worker-controller-9l0rr   1/1          Running       0          30s
	spark-worker-controller-g8kta   1/1          Running       0          30s
	
Perform `kubectl describe service spark-master --namespace=spark-cluster` to discover the `NodePort` at which spark is listening to you and
you're all set. Then it's just

    ./bin/spark-submit --name "sturdy-lambda" --class ru.rykov.rdd.App --master spark://<IP OF ANY OF YOUR K8S HOSTS>:<NodePort> \
	sturdy-lambda.jar file:///mnt/data/data.txt file:///mnt/data/map.groovy file:///mnt/data/reduce.groovy
	
I'll try to make this process less cumbersome in future.



