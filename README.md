# sso
- 引入maven依赖
```
<dependency>
    <groupId>com.zeroq6</groupId>
    <artifactId>sso-web-client</artifactId>
    <version>1.0.5</version>
</dependency>
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>dubbo</artifactId>
    <version>2.5.3</version>
    <exclusions>
        <exclusion>
            <artifactId>spring</artifactId>
            <groupId>org.springframework</groupId>
        </exclusion>
    </exclusions>
</dependency>
```
- spring配置文件
```
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:mvc="http://www.springframework.org/schema/mvc"
       xmlns:dubbo="http://code.alibabatech.com/schema/dubbo"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
    http://www.springframework.org/schema/beans/spring-beans-3.0.xsd
    
    
    http://www.springframework.org/schema/mvc
    http://www.springframework.org/schema/mvc/spring-mvc-3.0.xsd
    http://code.alibabatech.com/schema/dubbo http://code.alibabatech.com/schema/dubbo/dubbo.xsd">


    <!-- 消费方应用名，用于计算依赖关系，不是匹配条件，不要与提供方一样 -->
    <dubbo:application name="sso-consumer"/>

    <!-- 使用multicast广播注册中心暴露发现服务地址 -->
    <dubbo:registry address="N/A" />

    <!-- 生成远程服务代理，可以和本地bean一样使用demoService -->
    <dubbo:reference id="ssoServiceApi" timeout="5000" interface="com.zeroq6.sso.web.client.api.SsoServiceApi" version="1.0" url="dubbo://127.0.0.1:20880"/>


    <mvc:interceptors>
        <mvc:interceptor>
            <mvc:mapping path="/**"/>
            <bean id="springSsoInterceptor" class="com.zeroq6.sso.web.client.interceptor.LoginInterceptor">
                <property name="ssoServiceApi" ref="ssoServiceApi"/>
                <!--分组，不同分组登录状态隔离-->
                <!--<property name="groupId" value=""/>-->
                <!--如果配置/,则代表忽略所有,可加入disallow-->
                <!--<property name="ignoreUriPrefix" value="/"/>-->
            </bean>
        </mvc:interceptor>
    </mvc:interceptors>
</beans>
```
