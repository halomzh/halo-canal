package com.halo.canal.listener.event;

import com.halo.canal.listener.info.MessageReceiveInfo;
import org.springframework.context.ApplicationEvent;

/**
 * @author shoufeng
 */

public class MessageReceiveEvent extends ApplicationEvent {

	public MessageReceiveEvent(MessageReceiveInfo source) {
		super(source);
	}

}
