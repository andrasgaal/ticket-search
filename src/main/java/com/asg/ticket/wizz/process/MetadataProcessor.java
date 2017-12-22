package com.asg.ticket.wizz.process;

import com.asg.ticket.wizz.dto.Metadata;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import static java.util.UUID.randomUUID;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Component
public class MetadataProcessor extends BaseProcessor<String> {

    private static final String METADATA_URL = "https://wizzair.com/static/metadata.json";

    @Override
    public String process() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        log.info("Fetching metadata...");
        ResponseEntity<String> response = restTemplate.exchange(METADATA_URL, GET, entity, String.class);
        String metadata = GSON.fromJson(response.getBody(), Metadata.class).getApiUrl();
        log.info("Received metadata={}", metadata);

        reportMetadata(metadata);
        return metadata;
    }

    private void reportMetadata(String metadata) {
        try {
            HashMap<String, Object> source = new HashMap<>();
            source.put("searchDate", new Date());
            source.put("metadata", metadata);
            elasticClient.index(new IndexRequest("metadata", "metadata", randomUUID().toString()).source(source));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
