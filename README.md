# framework
## spring-data-jpawapper是什么?
一个封装了jpa操作的工具集

## 有哪些功能？

* crud
    *  方便使用hibernate hql sql语句,支持参数传递
    *  导出为一个html格式的文件，样式一点也不会丢失

## 怎么使用？

* spring boot 引入

```xml
    <dependency>
    	<groupId>com.github.liubingxu18</groupId>
		<artifactId>spring-data-jpawapper</artifactId>
		<version>1.0.0</version>
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
```
* 主函数引入注解

```xml
    @EnableJpaRepositories(repositoryFactoryBeanClass = BaseRepositoryFactoryBean.class)
```