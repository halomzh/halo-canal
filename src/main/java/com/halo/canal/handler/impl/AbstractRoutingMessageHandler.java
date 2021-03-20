package com.halo.canal.handler.impl;

import com.alibaba.otter.canal.protocol.Message;
import com.halo.canal.handler.MessageHandler;
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
import java.util.stream.Collectors;

/**
 * @author shoufeng
 */

public abstract class AbstractRoutingMessageHandler implements MessageHandler, ApplicationContextAware, InitializingBean {

	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	private ApplicationContext applicationContext;

	private List<MessageHandler> messageHandlerList;

	private List<String> handlerTypeList;

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
		for (MessageHandler messageHandler : messageHandlerList) {
			BoundValueOperations<String, String> valueOperations = handlerTypeValueOperationsMap.get(messageHandler.getHandlerType());
			if (!Boolean.parseBoolean(valueOperations.get())) {
				messageHandler.onMessage(message);
			}
			valueOperations.set(Boolean.TRUE.toString());
		}
		redisTemplate.delete(redisBatchIdKeyList);
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
	}

}
