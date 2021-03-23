# halo-canal

## 介绍

halo-canal是基于canal的数据增量同步服务。

## 原理

![总体设计.jpg](https://raw.githubusercontent.com/halomzh/pic/master/20210323214828.jpeg)

![原理流程.jpg](https://raw.githubusercontent.com/halomzh/pic/master/20210323173600.jpeg)

## 开始

### 基本配置

```yaml
spring:
  profiles:
    active: dev
  application:
    name: halo-canal-app
  elasticsearch: #elastic
    rest:
      uris: 127.0.0.1:9200
      username: elastic
      password: 123456
  redis:
    database: 0
    host: 127.0.0.1
    port: 6379
    password:
    timeout: 6000ms  # 连接超时时长（毫秒）
    jedis:
      pool:
        max-active: 1000  # 连接池最大连接数（使用负值表示没有限制）
        max-wait: -1ms      # 连接池最大阻塞等待时间（使用负值表示没有限制）
        max-idle: 10      # 连接池中的最大空闲连接
        min-idle: 5       # 连接池中的最小空闲连接

server:
  port: 8888
```

默认将监听到的message投递到ElasticSearch

如果不需要将处理后的数据投递到ElasticSearch可以移除ElasticSearchMessageHandler以及相关配置

### 数据同步服务相关配置

```yaml
canal:
  address-list: #canal服务端地址
    - 192.168.0.105:11111
  destination: example #canal目标服务
  username: #canal用户名
  password: #canal密码
  handler-type-list: #开启的消息处理器类型
    - elasticsearch
    - fdemo
  delay-pull-list: #拉取数据为空时，延时时间
    - 1
    - 2
    - 3
  elastic-search-info: #es接收canal投递的相关配置
    enable-index-init: false #是否启动时，初始化索引
    index-file-name-list: #需要被初始话的所有索引字段json文件文件名
      - demo_t_user.json
    index-name-id-field-name-map: #索引名称和主键id映射
      demo_t_user: id
```

### 启动

#### 启动canal deployer

![image-20210323220946928](https://raw.githubusercontent.com/halomzh/pic/master/20210323220948.png)

#### 启动ElasticSearch

![image-20210323221212438](https://raw.githubusercontent.com/halomzh/pic/master/20210323221213.png)

#### 启动halo-canal服务

![image-20210323221432450](https://raw.githubusercontent.com/halomzh/pic/master/20210323221433.png)

#### 测试同步服务是否够正常运行

##### 添加一条数据

![image-20210323222515877](https://raw.githubusercontent.com/halomzh/pic/master/20210323222517.png)

##### 处理器处理成功

![image-20210323222403984](https://raw.githubusercontent.com/halomzh/pic/master/20210323222405.png)

##### 消费成功，ack数据

![image-20210323222602422](https://raw.githubusercontent.com/halomzh/pic/master/20210323222603.png)

##### 查看数据投递情况

![image-20210323222704012](https://raw.githubusercontent.com/halomzh/pic/master/20210323222705.png)

##### 删除数据库数据

![image-20210323222755324](https://raw.githubusercontent.com/halomzh/pic/master/20210323222756.png)

##### 查看ElasticSearch是否删除成功

![image-20210323222849318](https://raw.githubusercontent.com/halomzh/pic/master/20210323222851.png)

### 自定义处理器

#### 编写处理器逻辑

```java
package com.halo.canal.handler.impl;

import com.alibaba.otter.canal.protocol.Message;
import com.halo.canal.handler.MessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


/**
 * @author shoufeng
 */

@Slf4j
@Component
public class FDemoMessageHandler implements MessageHandler {

   @Override
   public String getHandlerType() {
      return "fdemo";
   }

   @Override
   public void onMessage(Message message) {
      log.info("接收到数据: {}", message);
   }

}
```

#### 在配置中启动处理器

```yaml
handler-type-list: #开启的消息处理器类型
  - fdemo
```