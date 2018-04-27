package com.asg.ticket.wizz.fetch;

import com.asg.ticket.wizz.ElasticClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Data
@Component
public class BaseProcessor<R> {

    protected final HttpHeaders jsonHeaders = new HttpHeaders();
    protected final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    protected RestTemplate restTemplate;
    @Autowired
    protected ElasticClient elasticClient;

    public BaseProcessor() {
        jsonHeaders.setContentType(APPLICATION_JSON);
    }

}
