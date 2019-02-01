# framework
[GitHub地址](https://github.com/LiuBingXu18/framework) [maven地址](https://mvnrepository.com/artifact/com.github.liubingxu18/spring-data-jpawapper)
## spring-data-jpawapper是什么?
一个封装了jpa操作的工具集，且支持bean, repository, server, serverImpl, controller生成

## 有哪些功能？

* crud
    *  方便使用hibernate hql sql语句,支持参数传递
* 代码生成
    *  根据表生成代码

## 怎么使用？

* spring boot 引入

```xml
    <parent>
	<groupId>org.springframework.boot</groupId>
	<artifactId>spring-boot-starter-parent</artifactId>
	<version>2.1.1.RELEASE</version>
	<relativePath/> <!-- lookup parent from repository -->
    </parent>
    <dependency>
    	<groupId>com.github.liubingxu18</groupId>
	<artifactId>spring-data-jpawapper</artifactId>
	<version>1.0.7</version>
    </dependency>
```

* application.yml 引入

```xml
spring:
  jpa:
    show-sql: true
    database: mysql
    generate-ddl: true
    hibernate:
      ddl-auto: update
  datasource:
    url: jdbc:mysql://localhost:3306/dataBaseName?useUnicode=true&characterEncoding=utf-8&useSSL=true&serverTimezone=UTC
    username: root
    password: pwd
    type: com.alibaba.druid.pool.DruidDataSource
    driver-class-name: com.mysql.cj.jdbc.Driver
code-create: 
  #代码生成的数据库表 
  database-name: toolkit  
  #代码生成的bean包 
  bean-package: com.liubx.bean
  #代码生成的service包   
  service-package: com.liubx.web
  #代码生成的service实现类包     
  service-impl-package: com.liubx.web.server.impl
  #代码生成的repository包  
  repository-package: com.liubx.web.repository
  #代码生成的controller包  
  controller-package: com.liubx.web.controller  
  #是否启用代码生成
  enable: true
```
* 主函数引入注解

```xml
    @ComponentScan(basePackages = {"jpa.autocode"})  
    @EnableJpaRepositories(repositoryFactoryBeanClass = BaseRepositoryFactoryBean.class)
```
* 代码生成
    访问localhost:8080/code.html  
    ![在这里插入图片描述](https://img-blog.csdnimg.cn/20190201210506170.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzI3NDc0ODUx,size_16,color_FFFFFF,t_70)
