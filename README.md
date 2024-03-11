# 项目介绍
基于Spring Boot+Spring Cloud+MybatisPlus+RabbitMQ的算法刷题系统。系统功能：管理员可以创建、管理题目；用户可以搜索题目、查看题目、编写并提交代码以及查看答题结果。
# 主要工作
1.系统设计，整个系统分为用户模块、题目模块、判题模块、代码沙箱模块；
2.库表设计，并用MybatisX自动生成CRUD代码；
3.用模板方法模式实现代码沙箱，分别实现了java原生代码沙箱以及docker代码沙箱；
4.提供代码沙箱服务接口，并用API签名机制保证接口的安全性；
5.选用Spring Cloud Alibaba重构单体项目，使用Redis分布式Session存储用户登录信息，通过Nacos+OpenFeign实现各服务之间的相互调用；
6.使用RabbitMQ对判题操作进行异步化解耦。
# 附录
自定义代码沙箱实现：https://github.com/WangHenga/woj-codesandbox
