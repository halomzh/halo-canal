package com.halo.canal.client;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.protocol.Message;
import com.halo.canal.config.properties.CanalConfigProperties;
import com.halo.canal.exception.HaloCanalException;
import com.halo.canal.listener.event.MessageReceiveEvent;
import com.halo.canal.listener.info.MessageReceiveInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 客户端
 *
 * @author shoufeng
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
@Component
public class HaloCanalClient implements AutoCloseable, ApplicationContextAware, InitializingBean, DisposableBean {

	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	private CanalConfigProperties canalConfigProperties;
	private CanalConnector connector;
	private ApplicationContext applicationContext;
	private ExecutorService watchAckRollBackExecutorService = Executors.newFixedThreadPool(2);
	private static ArrayBlockingQueue<Long> ackBatchIdQueue = new ArrayBlockingQueue<>(10 * 1024);
	private static ArrayBlockingQueue<Long> rollBackBatchIdQueue = new ArrayBlockingQueue<>(10 * 1024);

	public void init() {
		canalConfigProperties = applicationContext.getBean(CanalConfigProperties.class);
		List<String> addressList = canalConfigProperties.getAddressList();
		if (CollectionUtils.isEmpty(addressList)) {
			throw new HaloCanalException("启动失败: 服务地址不能空");
		}
		List<InetSocketAddress> inetSocketAddressList = addressList.stream().map(address -> {
			String[] split = address.split(":");
			return new InetSocketAddress(split[0], Integer.parseInt(split[1]));
		}).collect(Collectors.toList());
		connector = CanalConnectors.newClusterConnector(inetSocketAddressList, canalConfigProperties.getDestination(), canalConfigProperties.getUsername(), canalConfigProperties.getPassword());
		connector.connect();
		connector.subscribe(".*\\..*");
		connector.rollback();
	}

	@SneakyThrows
	@Async
	public void start() {
		watchAckQueue();
		watchRollBackQueue();
		int emptyRunTimes = 0;
		List<Integer> delayPullList = canalConfigProperties.getDelayPullList();
		while (true) {
			Message message = connector.getWithoutAck(canalConfigProperties.getBatchSize(), canalConfigProperties.getTimeOutSeconds(), TimeUnit.SECONDS);
			long batchId = message.getId();
			int size = message.getEntries().size();
			if (batchId == -1 || size == 0) {
				int sleepSeconds = delayPullList.get(Math.min(emptyRunTimes, (delayPullList.size() - 1)));
				Thread.sleep(sleepSeconds * 1000);
				continue;
			}
			applicationContext.publishEvent(new MessageReceiveEvent(new MessageReceiveInfo(batchId, message, new Date())));
			emptyRunTimes = 0;
		}
	}

	public void watchAckQueue() {
		watchAckRollBackExecutorService.execute(() -> {
			while (true) {
				Long batchId = null;
				try {
					batchId = ackBatchIdQueue.take();
					connector.ack(batchId);
					log.info("ack成功: batchId[{}]", batchId);
				} catch (InterruptedException e) {
					log.error("ack失败: batchId[{}], \n {}", batchId, e);
				}
			}
		});
	}

	public void watchRollBackQueue() {
		watchAckRollBackExecutorService.execute(() -> {
			while (true) {
				Long batchId = null;
				try {
					batchId = rollBackBatchIdQueue.take();
					connector.rollback(batchId);
					log.info("rollBack成功: batchId[{}]", batchId);
				} catch (InterruptedException e) {
					log.error("rollBack失败: batchId[{}], \n {}", batchId, e);
				}
			}
		});
	}

	@SneakyThrows
	public static void ack(Long batchId) {
		if (ackBatchIdQueue.offer(batchId, 5, TimeUnit.SECONDS)) {
			log.info("加入ack队列成功: batchId[{}]", batchId);
		} else {
			log.error("加入ack队列失败: batchId[{}]", batchId);
		}
	}

	@SneakyThrows
	public static void rollBack(Long batchId) {
		if (rollBackBatchIdQueue.offer(batchId, 5, TimeUnit.SECONDS)) {
			log.info("加入rollBack队列成功: batchId[{}]", batchId);
		} else {
			log.error("加入rollBack队列失败: batchId[{}]", batchId);
		}
	}

	@Override
	public void close() throws IOException {
		if (ObjectUtils.isNotEmpty(connector)) {
			connector.disconnect();
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		init();
	}

	@Override
	public void destroy() throws Exception {
		close();
	}

}

