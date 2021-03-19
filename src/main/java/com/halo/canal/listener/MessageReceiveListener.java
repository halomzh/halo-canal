package com.halo.canal.listener;

import com.halo.canal.client.HaloCanalClient;
import com.halo.canal.handler.impl.DefaultRoutingMessageHandler;
import com.halo.canal.listener.event.MessageReceiveEvent;
import com.halo.canal.listener.info.MessageReceiveInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;

/**
 * @author shoufeng
 */

@Slf4j
public class MessageReceiveListener implements ApplicationListener<MessageReceiveEvent> {

	@Autowired
	private DefaultRoutingMessageHandler defaultRoutingMessageHandler;

	@Override
	public void onApplicationEvent(MessageReceiveEvent event) {
		MessageReceiveInfo messageReceiveInfo = (MessageReceiveInfo) event.getSource();

		try {
			//TODO 存在多个消息处理器由于某个消息处理器处理失败，消息回滚，其他消息处理器重复消费问题，需要处理操作成功的处理器的幂等问题
			defaultRoutingMessageHandler.onMessage(messageReceiveInfo.getMessage());
			HaloCanalClient.ack(messageReceiveInfo.getBatchId());
		} catch (Exception e) {
			log.error("执行消息处理失败: ", e);
			HaloCanalClient.rollBack(messageReceiveInfo.getBatchId());
		}

	}

}
