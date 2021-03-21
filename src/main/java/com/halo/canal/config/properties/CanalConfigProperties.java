package com.halo.canal.config.properties;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * canal配置文件
 *
 * @author shoufeng
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Component
@ConfigurationProperties(prefix = "canal")
public class CanalConfigProperties {

	/**
	 * canal服务端地址
	 */
	private List<String> addressList = new ArrayList<>();

	/**
	 * canal目标服务
	 */
	private String destination = "";

	/**
	 * canal用户名
	 */
	private String username = "";

	/**
	 * canal密码
	 */
	private String password = "";

	/**
	 * 批量获取消息个数
	 */
	private int batchSize = 1000;

	/**
	 * 获取数据超时
	 */
	private long timeOutSeconds = 1;

	/**
	 * 拉取数据为空时，延时时间
	 */
	private List<Integer> delayPullList = Lists.newArrayList(1, 3, 5, 7, 10);

	/**
	 * 激活使用的消息处理器类型
	 */
	private List<String> handlerTypeList = Lists.newArrayList();

	/**
	 * es接收canal投递的相关配置
	 */
	private ElasticSearchInfo elasticSearchInfo = new ElasticSearchInfo();

	@Data
	public static class ElasticSearchInfo {

		/**
		 * 索引字段设置json文件存放文件夹路径
		 */
		private String indexPrefix = "indices";

		/**
		 * 是否启动时，初始化索引
		 */
		private boolean enableIndexInit = false;

		/**
		 * 需要被初始话的所有索引字段json文件文件名
		 */
		private List<String> indexFileNameList = new ArrayList<>();

		/**
		 * 索引名称和主键id映射
		 */
		private Map<String, String> indexNameIdFieldNameMap = new HashMap<>();

	}
}
