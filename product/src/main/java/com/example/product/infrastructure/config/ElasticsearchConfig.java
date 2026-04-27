package com.example.product.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;

@Configuration
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    @Value("${cloud.elasticsearch.uris:http://localhost:9200}")
    private String elasticsearchUri;

    @Override
    public ClientConfiguration clientConfiguration() {
        String hostAndPort = elasticsearchUri
                .replace("https://", "")
                .replace("http://", "");
        return ClientConfiguration.builder()
                .connectedTo(hostAndPort)
                .build();
    }
}
