# SpringCloud

## Eureka
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