package com.halo.canal.config;

import com.google.common.collect.Lists;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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
public class CanalConfig {

	private List<String> addressList = new ArrayList<>();

	private String destination = "";

	private String username = "";

	private String password = "";

	private int batchSize = 1000;

	private long timeOutSeconds = 1;

	private List<Integer> delayPullList = Lists.newArrayList(1, 3, 5, 7, 10);

	private List<String> handlerTypeList = Lists.newArrayList();

}
