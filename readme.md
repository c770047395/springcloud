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

### 2.2 自定义Ribbon的负载均衡模式
Ribbon中有一个IRule接口定义了如何实现负载均衡，默认是轮询模式，

除了Ribbon中已经定义的策略之外，我们还可以自定义策略（注意，自定义策略时，
不能被ComponentScan注解扫描，否则IRule会变成全局的）

1.在主启动类上加上RibbonClient注解
```java
@SpringBootApplication
@EnableEurekaClient
//在微服务启动的时候就能加载自定义的ribbon类
@RibbonClient(name="SPRINGCLOUD-PROVIDER-DEPT",configuration = CpRule.class)
public class DeptConsumer_80 {
    public static void main(String[] args) {
        SpringApplication.run(DeptConsumer_80.class,args);
    }
```
2.编写IRule类（要在ComponentScan之外）
```java
@Configuration
public class CpRule  {

    @Bean
    public IRule myRule(){
        return new CpRandomRule();
    }
}
```
注：这里的IRule可以使用Ribbon自带的策略如RandomRule，RoundRule等，也可以自定义

3.自定义注解（见CpRandomRule类）


## 3.Feign
Feign对Ribbon进行了封装，以接口注入的形式提供给Controller调用。

1.引入Feign依赖

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-feign</artifactId>
    <version>1.4.7.RELEASE</version>
</dependency>
```

2.编写接口

```java
@FeignClient(value = "SPRINGCLOUD-PROVIDER-DEPT")
public interface DeptClientService {
    @GetMapping("/dept/get/{id}")
    public Dept queryById(@PathVariable("id") Long id);

    @GetMapping("/dept/list")
    public List<Dept> queryAll();

    @GetMapping("/dept/add")
    public boolean addDept(Dept dept);

    @GetMapping("/dept/discovery")
    public Object dis();
}
```
注：其中@FeignClient注解通过value值去Eureka注册中心中获取对应的服务，也可实现负载均衡

3.在消费者控制器中注入，并通过接口调用

```java
@RestController
public class DeptConsumerController {

    @Autowired
    private DeptClientService service;

    @RequestMapping("consumer/dept/get/{id}")
    public Dept get(@PathVariable("id") Long id){
        return this.service.queryById(id);
    }

    @RequestMapping("consumer/dept/add")
    public boolean add(Dept dept){
        System.out.println(dept);
        return this.service.addDept(dept);
    }

    @RequestMapping("consumer/dept/list")
    public List<Dept> list(){
        return this.service.queryAll();
    }

    @RequestMapping("/dept/discovery")
    public Object discovery(){
        return this.service.dis();
    }
}
```

4.主启动类中设置Feign的扫描包

```java
@SpringBootApplication
@EnableEurekaClient
@EnableFeignClients(basePackages = {"com.cp.springcloud"})
public class FeignDeptConsumer_80 {
    public static void main(String[] args) {
        SpringApplication.run(FeignDeptConsumer_80.class,args);
    }
}

```


## 4.hystrix
对于微服务来说，服务之间存在调用链，当这条链上一个服务崩了的时候，
在高并发情况下，可能导致服务器瞬间达到饱和状态，无法接受新的请求，
这是我们不能容忍的，所以在一个微服务出现故障的时候，我们需要一个容灾机制，
hystrix有效的帮我们解决了这个问题

## 4.1 服务熔断
由于用户请求失误（如请求id为10的用户，但是系统中无此用户）时，调用
另外一个方法返回给用户错误的结果而不直接抛出异常。

1.添加hystrix依赖
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-hystrix</artifactId>
    <version>1.4.7.RELEASE</version>
</dependency>
```

2.为需要熔断的服务加上@HystrixCommand注解并设置备选方案
```java
@RequestMapping("/dept/get/{id}")
@HystrixCommand(fallbackMethod = "hystrixGet")
public Dept get(@PathVariable("id") Long id){
    Dept dept = deptService.queryById(id);
    if (dept == null){
        throw new RuntimeException("id=》"+id+"，不存在该用户或者信息无法找到");
    }
    return dept;
}

//备选方案
public Dept hystrixGet(@PathVariable("id") Long id){
    return new Dept()
            .setDeptno(id)
            .setDname("id=》"+id+"，没有对应的信息，null--@hystrix")
            .setDb_source("没有数据库信息");
}
```

3.在主启动类上开启熔断支持
```java
@SpringBootApplication
@EnableEurekaClient
@EnableDiscoveryClient
//添加熔断支持
@EnableCircuitBreaker
public class HystrixDeptProvider_8001 {
    public static void main(String[] args) {
        SpringApplication.run(HystrixDeptProvider_8001.class,args);
    }
}
```

### 4.2 服务降级

服务降级与服务熔断不同，服务降级是整体的概念，是在客户端方面定义降级规则
，一个服务被关闭之后，用户再访问的时候则返回降级后（默认）的信息，
而不会直接报错，与直接关闭不同，好歹能用

1.编写降级工厂
```java
//降级
@Component
public class DeptClientServiceFallbackFactory implements FallbackFactory {

    @Override
    public DeptClientService create(Throwable throwable) {
        return new DeptClientService() {
            @Override
            public Dept queryById(Long id) {
                return new Dept()
                        .setDeptno(id)
                        .setDname("id=》"+id+"没有对应的信息，服务已被降级关闭")
                        .setDb_source("没有数据");
            }

            @Override
            public List<Dept> queryAll() {
                return null;
            }

            @Override
            public boolean addDept(Dept dept) {
                return false;
            }

            @Override
            public Object dis() {
                return null;
            }
        };
    }
}

```

2.在FeignClient注解上加入fallbackFactory并指定类

```java
@FeignClient(value = "SPRINGCLOUD-PROVIDER-DEPT",fallbackFactory = DeptClientServiceFallbackFactory.class)
```

3.当服务被关闭时，则会返回降级提示（默认值）。
