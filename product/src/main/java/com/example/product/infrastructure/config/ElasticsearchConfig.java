package com.example.product.infrastructure.config;

import co.elastic.clients.json.JsonpMapper;
import co.elastic.clients.json.jackson.Jackson3JsonpMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    @Value("${ELASTICSEARCH_URI:http://localhost:9200}")
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

    @Override
    public JsonpMapper jsonpMapper() {
        JsonMapper mapper = JsonMapper.builder()
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
        return new Jackson3JsonpMapper(mapper);
    }
}
