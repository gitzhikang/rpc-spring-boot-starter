# Overview
This is an RPC framework referenced from [Guide Rpc Framework],but I have made some changes to make it easier to use.
- I used spring boot auto configuration to load the resources, so you don't need to configure the resources manually.
- I have added a `@RpcServer` annotation to mark the service application, and added a `@RpcClient` annotation to mark the service reference, which will load needed resources automatically.
- I have give the caller right to choose async or sync call to support asynchronous operation orchestration.

# Getting Started
## How to run sample project
- set up zookeeper server as the service registry
```dockerfile
docker run -d -p 2181:2181 --name zookeeper zookeeper
```
- clone the project
```shell
git clone git@github.com:gitzhikang/rpc-spring-boot-starter.git
```
- compile the project
```shell
cd rpc-spring-boot-starter
mvn clean install
```
- run the test-server
```shell
cd test-server
mvn spring-boot:run
```
- run the test-client
```shell
cd test-client
mvn spring-boot:run
```
- visit the test-client
```shell
curl http://localhost:8081/sayHello/zhikang
```
## How to include the starter in your project
- add the dependency in your pom.xml
```xml
<dependency>
    <groupId>com.github.zhikang</groupId>
    <artifactId>rpc-spring-boot-starter</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```
- add the configuration in your application.properties
```properties
rpc.config.defaultZookeeperAddress=localhost:2181
rpc.config.zkRegisterRootPath=/rpc-framework
rpc.config.baseSleepTime=5000
rpc.config.maxRetries=3
```
- add the `@RpcServer` and `@RpcScan` annotation to your service application
```java
@SpringBootApplication
@RpcServer
@RpcScan(basePackage = {"com.zeke.testserver"})
public class TestServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestServerApplication.class, args);
    }

}
```
- add the `@RpcClient` annotation to your client application
```java
@SpringBootApplication
@RpcClient
public class TestClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(TestClientApplication.class, args);
    }

}
```
- add the `@RpcReference` annotation to your service reference in client application service
```java
@RestController
public class TestController {

    @RpcReference
    private HelloService helloService;

    @GetMapping("/sayHello/{name}")
    public String sayHello(@PathVariable String name) {
        return helloService.sayHello(name);
    }

}
```
- add the `@RpcService` annotation to your service implementation in server application service
```java
@RpcService(group = "test1", version = "version1")
public class UserServiceImpl implements IUserService {

    public String sayHello(String name) {
        return "Hello "+ name;
    }
}
```
## How to use Async call and Sync call
- add `async = true` in `@RpcReference`, get result from `AsyncResult.getCurrentResult()`
```java
@Service
public class UserBusiness {

    @RpcReference(version = "version1", group = "test1",async = true)
    IUserService userService;


    public String sayHello(String name) throws ExecutionException, InterruptedException {
        userService.sayHello(name);
        CompletableFuture<Object> currentResult = AsyncResult.getCurrentResult();
        return (String) currentResult.get();
    }
}
```



### Reference Documentation

* [Guide Rpc Framework](https://github.com/Snailclimb/guide-rpc-framework)

