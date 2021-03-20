package com.halo.canal.handler;

import com.alibaba.otter.canal.client.adapter.support.Dml;
import com.alibaba.otter.canal.protocol.Message;

/**
 * 消息处理
 *
 * @author shoufeng
 */

public interface MessageHandler {

	/**
	 * 获取处理类型
	 *
	 * @return 处理类型
	 */
	String getHandlerType();

	/**
	 * 消息到达
	 *
	 * @param message 消息
	 */
	void onMessage(Message message);

	/**
	 * insert消息处理
	 *
	 * @param dml DML操作转换对象
	 */
	default void onInsertDml(Dml dml) {
	}

	/**
	 * update消息处理
	 *
	 * @param dml DML操作转换对象
	 */
	default void onUpdateDml(Dml dml) {
	}

	/**
	 * delete消息处理
	 *
	 * @param dml DML操作转换对象
	 */
	default void onDeleteDml(Dml dml) {
	}

}
