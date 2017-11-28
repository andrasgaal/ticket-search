package com.asg.ticket.wizz.process;

import com.asg.ticket.wizz.dto.Metadata;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
public class MetadataProcessor implements Processor<Object> {

    private static final String METADATA_URL = "https://wizzair.com/static/metadata.json";

    private final RestTemplate restClient;
    private final Processor<String> nextProcessor;

    @Autowired
    public MetadataProcessor(RestTemplate restClient, @Qualifier("searchResultProcessor") Processor<String> nextProcessor) {
        this.restClient = restClient;
        System.out.println("Metadata processor created");
        this.nextProcessor = nextProcessor;
        process(null);
    }

    @Override
    public void process(Object input) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restClient.exchange(METADATA_URL, GET, entity, String.class);
        System.out.println(response.getBody());
        Metadata metadata = new Gson().fromJson(response.getBody(), Metadata.class);

        nextProcessor.process(metadata.getApiUrl());
    }

}
