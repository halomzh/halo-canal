package com.halo.canal.handler.impl;

import com.halo.canal.config.properties.CanalConfigProperties;
import com.halo.canal.handler.MessageHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author shoufeng
 */

@Component
public class DefaultRoutingMessageHandler extends AbstractRoutingMessageHandler {

	public static final String HANDLER_TYPE = "default";

	@Autowired
	private CanalConfigProperties canalConfigProperties;

	@Override
	public boolean match(MessageHandler messageHandler) {
		return canalConfigProperties.getHandlerTypeList().contains(messageHandler.getHandlerType());
	}

	@Override
	public String getHandlerType() {
		return HANDLER_TYPE;
	}

}
