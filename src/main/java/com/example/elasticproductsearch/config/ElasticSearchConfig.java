package com.example.elasticproductsearch.config;

import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.apache.http.HttpHost;

@Configuration
public class ElasticSearchConfig {
    @Value("${elasticsearch.filter.enabled:false}")  // Фильтр отключён по умолчанию
    private boolean filterEnabled;

    public boolean isFilterEnabled() {
        return filterEnabled;
    }
    @Bean
    public RestHighLevelClient client() {

        return new RestHighLevelClient(
                RestClient.builder(new HttpHost("localhost", 9200, "http")));

}

}