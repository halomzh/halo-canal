package com.halo.canal.listener;

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

		defaultRoutingMessageHandler.onMessage(messageReceiveInfo.getMessage());

	}

}
