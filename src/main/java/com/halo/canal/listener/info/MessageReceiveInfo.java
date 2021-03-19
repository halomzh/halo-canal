package com.halo.canal.listener.info;

import com.alibaba.otter.canal.protocol.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author shoufeng
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageReceiveInfo {

	private Long batchId;

	private Message message;

	private Date receiveDate;

}
