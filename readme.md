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