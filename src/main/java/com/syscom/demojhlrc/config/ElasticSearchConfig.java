package com.syscom.demojhlrc.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticSearchConfig {

	@Value("${spring.elasticsearch.rest.host}")
	private String elasticSearchHost;

	@Value("${spring.elasticsearch.rest.port}")
	private int elasticSearchPort;

	@Value("${spring.elasticsearch.rest.username}")
	private String elasticSearchClientUsername;

	@Value("${spring.elasticsearch.rest.password}")
	private String elasticSearchClientPassword;

	@Bean(destroyMethod = "close")
	public RestHighLevelClient client() {
		RestHighLevelClient client = new RestHighLevelClient(
				RestClient.builder(new HttpHost(elasticSearchHost, elasticSearchPort)));
		return client;
	}

}