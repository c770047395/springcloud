# SpringCloud

## 1.Eureka
 Eureka是注册中心，提供服务的注册与发现，Eureka服务器需要的依赖如下
 
```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-eureka-server</artifactId>
        <version>1.4.7.RELEASE</version>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-devtools</artifactId>
    </dependency>
</dependencies>
```

yml配置为
```yaml
server:
  port: 7001
#Eureka配置
eureka:
  instance:
    hostname: localhost
  client:
    register-with-eureka: false #是否向eureka注册中心注册自己
    fetch-registry: false #如果为false则表示自己为注册中心
    service-url:
      defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
```

其服务器启动的核心注解为@EnableEurekaServer
```java
@SpringBootApplication
@EnableEurekaServer //服务端的启动类，可以接受别人注册进来~
public class EurekaServer_7001 {
    public static void main(String[] args) {
        SpringApplication.run(EurekaServer_7001.class,args);
    }
}
```

### 1.1Eureka生产者（服务提供者）
Eureka生产者同样需要配置依赖
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-eureka</artifactId>
    <version>1.4.7.RELEASE</version>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

然后配置配置文件
```yaml
#Eureka的配置,服务注册到哪里
eureka:
  client:
    service-url:
     defaultZone: http://localhost:7001/eureka/
  instance:
    instance-id: springcloud-provider-dept-8001 #修改Eureka的默认描述信息

info:
  app.name: cp-springcloud
  company.name: blog.cp.com
```

最后加上注解即可使用
```java
@SpringBootApplication
@EnableEurekaClient
@EnableDiscoveryClient
public class DeptProvider_8001 {
    public static void main(String[] args) {
        SpringApplication.run(DeptProvider_8001.class,args);
    }
}
```

### 1.2Eureka集群的配置

在配置Eureka服务器的时候，可以在defaultZone配置中加上多个地址，就可以实现集群

```yaml
server:
  port: 7001
#Eureka配置
eureka:
  instance:
    hostname: eureka7001.com
  client:
    register-with-eureka: false #是否向eureka注册中心注册自己
    fetch-registry: false #如果为false则表示自己为注册中心
    service-url:
#      单机：defaultZone: http://${eureka.instance.hostname}:${server.port}/eureka/
      defaultZone: http://eureka7002.com:7002/eureka/,http://eureka7003.com:7003/eureka/
```

## 2.Ribbon

Ribbon在Springcloud中起到负载均衡的作用，实现了客户端的负载均衡，
是由客户端向Eureka注册中心先获取可用的服务列表再进行的负载均衡，
与Nginx服务端的反向代理不同

### 2.1 Ribbon的配置

首先同样是在*客户端*导入依赖
```xml
<!--Ribbon-->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-ribbon</artifactId>
    <version>1.4.7.RELEASE</version>
</dependency>
<!--Eureka-->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-eureka</artifactId>
    <version>1.4.7.RELEASE</version>
</dependency>
```
此时由于客户端中加入了Eureka，需要在启动类上配置
```java
@SpringBootApplication
@EnableEurekaClient
public class DeptConsumer_80 {
    public static void main(String[] args) {
        SpringApplication.run(DeptConsumer_80.class,args);
    }
}

```
然后向配置文件中添加注册中心
```yaml
#Eureka配置
eureka:
  client:
    register-with-eureka: false #不向Eureka中注册自己
    service-url:
      defaultZone: http://eureka7001.com:7001/eureka/,http://eureka7002.com:7002/eureka/,http://eureka7003.com:7003/eureka/
```

修改RestTemplate的注入方式
```java
//配置负载均衡实现RestTemplate
@Bean
@LoadBalanced  //Ribbon
public RestTemplate getRestTemplate(){
    return new RestTemplate();
}
```

修改Controller中的服务提供者前缀变成Eureka注册中心所提供的服务地址
```java
    @Autowired
    private RestTemplate restTemplate; //提供多种便捷访问http服务的方法，简单的restful服务模板

    //private static final String REST_URL_PREFIX="http://localhost:8001";
    //通过Ribbon实现，地址应该是变量，通过服务访问
    private static final String REST_URL_PREFIX="http://SPRINGCLOUD-PROVIDER-DEPT";
```
