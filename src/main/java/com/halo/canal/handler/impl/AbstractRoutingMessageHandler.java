package com.halo.canal.handler.impl;

import com.alibaba.otter.canal.protocol.Message;
import com.halo.canal.handler.MessageHandler;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author shoufeng
 */

public abstract class AbstractRoutingMessageHandler implements MessageHandler, ApplicationContextAware, InitializingBean {

	private ApplicationContext applicationContext;

	private List<MessageHandler> messageHandlerList;

	public void filter(List<MessageHandler> messageHandlerList) {
		this.messageHandlerList = messageHandlerList.stream().filter(this::match).collect(Collectors.toList());
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
		for (MessageHandler messageHandler : messageHandlerList) {
			messageHandler.onMessage(message);
		}
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
