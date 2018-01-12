package com.asg.ticket.wizz.process;

import com.asg.ticket.wizz.ElasticClient;
import com.google.gson.Gson;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Data
@Component
public abstract class BaseProcessor<R> {

    protected final Gson GSON = new Gson();
    protected final HttpHeaders jsonHeaders = new HttpHeaders();
    @Autowired
    protected RestTemplate restTemplate;
    @Autowired
    protected ElasticClient elasticClient;

    @PostConstruct
    private void init() {
        jsonHeaders.setContentType(APPLICATION_JSON);
    }

    abstract R process();
}
