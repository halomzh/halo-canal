package com.halo.canal.handler.impl;

import com.alibaba.otter.canal.client.adapter.support.Dml;
import com.alibaba.otter.canal.client.adapter.support.MessageUtil;
import com.alibaba.otter.canal.protocol.Message;
import com.halo.canal.config.properties.CanalConfigProperties;
import com.halo.canal.handler.MessageHandler;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * elasticsearch消息处理器
 *
 * @author shoufeng
 */

@Slf4j
@Component
public class ElasticSearchMessageHandler implements MessageHandler, InitializingBean {

	public static final String HANDLER_TYPE = "elasticsearch";

	@Autowired
	private CanalConfigProperties canalConfigProperties;

	@Autowired
	private RestHighLevelClient restHighLevelClient;

	@Override
	public String getHandlerType() {
		return HANDLER_TYPE;
	}

	@SneakyThrows
	@Override
	public void onMessage(Message message) {
		List<Dml> dmlList = MessageUtil.parse4Dml(canalConfigProperties.getDestination(), message);
		log.info("dmlList: [{}]", dmlList);
		for (Dml dml : dmlList) {
			//INSERT UPDATE DELETE
			switch (dml.getType()) {
				case "INSERT": {
					onInsertDml(dml);
					break;
				}
				case "UPDATE": {
					onUpdateDml(dml);
					break;
				}
				case "DELETE": {
					onDeleteDml(dml);
					break;
				}
				default: {
					log.warn("未知数据类型的Dml: {}", dml);
				}
			}
		}
	}

	private String generateIndexNameByDml(Dml dml) {
		String database = dml.getDatabase();
		String table = dml.getTable();

		return database + "_" + table;
	}

	private String getIndexIdFieldNameByIndexName(String indexName) {
		Map<String, String> indexNameIdFieldNameMap = canalConfigProperties.getElasticSearchInfo().getIndexNameIdFieldNameMap();

		return indexNameIdFieldNameMap.get(indexName);
	}

	private Map<String, Object> generateFieldNameFieldValueMap(Dml dml) {
		Map<String, Object> fieldNameFieldValueMap = new HashMap<>();
		dml.getData().forEach(fieldNameFieldValueMap::putAll);

		return fieldNameFieldValueMap;
	}

	private String getIndexIdValue(Map<String, Object> fieldNameFieldValueMap, String indexIdFieldName) {

		return String.valueOf(fieldNameFieldValueMap.get(indexIdFieldName));
	}

	@SneakyThrows
	private void onInsertOrUpdateDml(Dml dml) {
		String indexName = generateIndexNameByDml(dml);
		IndexRequest indexRequest = new IndexRequest();
		indexRequest.index(indexName);
		Map<String, Object> fieldNameFieldValueMap = generateFieldNameFieldValueMap(dml);
		String indexIdFieldName = getIndexIdFieldNameByIndexName(indexName);
		indexRequest.id(getIndexIdValue(fieldNameFieldValueMap, indexIdFieldName));
		indexRequest.source(fieldNameFieldValueMap);
		IndexResponse indexResponse = restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);

		log.info("新增/修改document: {}", indexResponse);
	}

	@Override
	public void onInsertDml(Dml dml) {
		onInsertOrUpdateDml(dml);
	}

	@Override
	public void onUpdateDml(Dml dml) {
		onInsertOrUpdateDml(dml);
	}

	@SneakyThrows
	@Override
	public void onDeleteDml(Dml dml) {
		String indexName = generateIndexNameByDml(dml);
		String idFieldName = getIndexIdFieldNameByIndexName(indexName);
		Map<String, Object> fieldNameFieldValueMap = generateFieldNameFieldValueMap(dml);
		String indexIdValue = getIndexIdValue(fieldNameFieldValueMap, idFieldName);
		DeleteRequest deleteRequest = new DeleteRequest();
		deleteRequest.index(indexName);
		deleteRequest.id(indexIdValue);
		DeleteResponse deleteResponse = restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);

		log.info("删除document: {}", deleteResponse);
	}

	@SneakyThrows
	public void createIndex(String filePathPrefix, @NonNull String fileName) {
		CreateIndexRequest createIndexRequest = new CreateIndexRequest(fileName.split("\\.")[0]);

		String mappingSource;
		try (InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(filePathPrefix + "/" + fileName)) {
			assert inputStream != null;
			mappingSource = IOUtils.toString(inputStream);
		}

		createIndexRequest.mapping(mappingSource, XContentType.JSON);
		CreateIndexResponse createIndexResponse = restHighLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);

		log.info("创建index: {}", createIndexResponse);
	}

	public void createAllIndices() {
		canalConfigProperties
				.getElasticSearchInfo()
				.getIndexFileNameList()
				.forEach(indexFileName -> {
					createIndex(canalConfigProperties.getElasticSearchInfo().getIndexPrefix(), indexFileName);
				});
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		if (canalConfigProperties.getElasticSearchInfo().isEnableIndexInit()) {
			createAllIndices();
		}
	}

}
