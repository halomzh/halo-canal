package com.halo.canal.handler.impl;

import com.alibaba.otter.canal.client.adapter.support.Dml;
import com.alibaba.otter.canal.client.adapter.support.MessageUtil;
import com.alibaba.otter.canal.protocol.Message;
import com.halo.canal.config.CanalConfig;
import com.halo.canal.handler.MessageHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * elasticsearch消息处理器
 *
 * @author shoufeng
 */

@Slf4j
@Component
public class ElasticSearchMessageHandler implements MessageHandler {

	public static final String HANDLER_TYPE = "elasticsearch";

	@Autowired
	private CanalConfig canalConfig;

	@Override
	public String getHandlerType() {
		return HANDLER_TYPE;
	}

	@Override
	public void onMessage(Message message) {
		log.info("elasticSearch处理器收到消息: {}", message);
		List<Dml> dmlList = MessageUtil.parse4Dml(canalConfig.getDestination(), message);
	}
}
