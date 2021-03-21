package com.halo.canal.handler.task;

import com.alibaba.otter.canal.protocol.Message;
import com.google.common.collect.Lists;
import com.halo.canal.handler.MessageHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.data.redis.core.BoundValueOperations;

import java.util.List;
import java.util.Map;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;

/**
 * @author shoufeng
 */

@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
@AllArgsConstructor
public class MessageHandlerTask extends RecursiveTask<Boolean> {

	private Message message;
	private List<MessageHandler> messageHandlerList;
	private Map<String, BoundValueOperations<String, String>> handlerTypeValueOperationsMap;

	@Override
	protected Boolean compute() {
		if (CollectionUtils.isEmpty(messageHandlerList)) {
			return true;
		}
		if (messageHandlerList.size() == 1) {
			MessageHandler messageHandler = messageHandlerList.get(0);
			try {
				messageHandler.onMessage(message);
				BoundValueOperations<String, String> valueOperations = handlerTypeValueOperationsMap.get(messageHandler.getHandlerType());
				valueOperations.set(Boolean.TRUE.toString());
				return true;
			} catch (Exception e) {
				log.info("消息处理器[{}]处理消息[{}]失败: {}", messageHandler.getHandlerType(), message, e);
				return false;
			}

		}
		List<MessageHandlerTask> messageHandlerTaskList = Lists.partition(messageHandlerList, 1).stream().map(messageHandlerListTemp -> new MessageHandlerTask(message, messageHandlerListTemp, handlerTypeValueOperationsMap)).collect(Collectors.toList());

		boolean result = true;
		for (MessageHandlerTask messageHandlerTask : messageHandlerTaskList) {
			result = result && messageHandlerTask.fork().join();
		}

		return result;
	}

}
