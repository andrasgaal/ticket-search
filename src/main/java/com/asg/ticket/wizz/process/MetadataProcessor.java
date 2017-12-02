package com.asg.ticket.wizz.process;

import com.asg.ticket.wizz.dto.Metadata;
import com.google.gson.Gson;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component
public class MetadataProcessor extends BaseProcessor implements Processor<String> {

    private static final String METADATA_URL = "https://wizzair.com/static/metadata.json";

    @Override
    public String process() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(METADATA_URL, GET, entity, String.class);
        return new Gson().fromJson(response.getBody(), Metadata.class).getApiUrl();
    }

}
