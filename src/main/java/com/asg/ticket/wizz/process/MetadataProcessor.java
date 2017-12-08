package com.asg.ticket.wizz.process;

import com.asg.ticket.wizz.dto.Metadata;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

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
        return metadata;
    }

}
