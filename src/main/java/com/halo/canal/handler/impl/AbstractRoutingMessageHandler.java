package com.halo.canal.handler.impl;

import com.alibaba.otter.canal.protocol.Message;
import com.halo.canal.client.HaloCanalClient;
import com.halo.canal.handler.MessageHandler;
import com.halo.canal.handler.task.MessageHandlerTask;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author shoufeng
 */

@Slf4j
public abstract class AbstractRoutingMessageHandler implements MessageHandler, ApplicationContextAware, InitializingBean {

	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	private ApplicationContext applicationContext;

	private List<MessageHandler> messageHandlerList;

	private List<String> handlerTypeList;

	private ForkJoinPool messageHandlerForkJoinPool;

	public static final String CANAL_MESSAGE_KEY_PREFIX = "canal:message:";

	public void filter(List<MessageHandler> messageHandlerList) {
		this.messageHandlerList = messageHandlerList.stream().filter(this::match).collect(Collectors.toList());
		handlerTypeList = this.messageHandlerList.stream().map(MessageHandler::getHandlerType).collect(Collectors.toList());
	}

	/**
	 * 匹配
	 *
	 * @param messageHandler 消息处理器
	 * @return 是否匹配
	 */
	public abstract boolean match(MessageHandler messageHandler);

	@SneakyThrows
	@Override
	public void onMessage(Message message) {
		Map<String, BoundValueOperations<String, String>> handlerTypeValueOperationsMap = new HashMap<>();
		List<String> redisBatchIdKeyList = new ArrayList<>();
		handlerTypeList.forEach(handlerType -> {
			String key = CANAL_MESSAGE_KEY_PREFIX + handlerType;
			redisBatchIdKeyList.add(key);
			BoundValueOperations<String, String> valueOperations = redisTemplate.boundValueOps(key);
			handlerTypeValueOperationsMap.put(handlerType, valueOperations);
			valueOperations.setIfAbsent(Boolean.FALSE.toString());
		});

		List<MessageHandler> nextMessageHandlerList = messageHandlerList.stream().filter(messageHandler -> !Boolean.parseBoolean(handlerTypeValueOperationsMap.get(messageHandler.getHandlerType()).get())).collect(Collectors.toList());
		ForkJoinTask<Boolean> booleanForkJoinTask = messageHandlerForkJoinPool.submit(new MessageHandlerTask(message, nextMessageHandlerList, handlerTypeValueOperationsMap));
		if (booleanForkJoinTask.get(5, TimeUnit.MINUTES)) {
			redisTemplate.delete(redisBatchIdKeyList);
			log.info("所有消息处理器处理成功: message[{}]", message);
			HaloCanalClient.ack(message.getId());
			return;
		}
		log.info("存在部分消息处理器处理异常: message[{}]", message);
		HaloCanalClient.rollBack(message.getId());
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		Map<String, MessageHandler> beanNameMessageHandlerMap = applicationContext.getBeansOfType(MessageHandler.class);
		messageHandlerList = beanNameMessageHandlerMap.keySet().stream().map(beanNameMessageHandlerMap::get).filter(messageHandler -> !(messageHandler instanceof AbstractRoutingMessageHandler)).collect(Collectors.toList());
		filter(messageHandlerList);
		messageHandlerForkJoinPool = new ForkJoinPool(this.messageHandlerList.size());
	}

}
