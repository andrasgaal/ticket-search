package com.asg.ticket.wizz;

import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

import static java.util.UUID.randomUUID;

@Component
public class ElasticClient {

    @Autowired
    private RestHighLevelClient elasticClient;

    public void report(String index, Map source) {
        try {
            elasticClient.index(new IndexRequest(index, index,
                    randomUUID().toString()).source(source));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void report(String index, String source) {
        try {
            elasticClient.index(new IndexRequest(index, index,
                    randomUUID().toString()).source(source, XContentType.JSON));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
