package com.asg.ticket.wizz.process;

import com.asg.ticket.wizz.dto.Metadata;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Slf4j
@Component
public class MetadataProcessor extends BaseProcessor<Metadata> {

    private static final String METADATA_URL = "https://wizzair.com/static/metadata.json";

    @Override
    public Metadata process() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        log.info("Fetching metadata...");
        ResponseEntity<String> response = restTemplate.exchange(METADATA_URL, GET, entity, String.class);
        Metadata metadata = GSON.fromJson(response.getBody(), Metadata.class);
        log.info("Received metadata={}", metadata);

        reportMetadata(metadata);
        return metadata;
    }

    private void reportMetadata(Metadata metadata) {
        HashMap<String, Object> source = new HashMap<>();
        source.put("searchDate", new Date());
        source.put("metadata", metadata.getApiUrl());
        elasticClient.report("metadata", source);
    }

}
