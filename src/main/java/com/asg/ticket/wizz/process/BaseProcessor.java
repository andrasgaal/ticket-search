package com.asg.ticket.wizz.process;

import com.google.gson.Gson;
import lombok.Data;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Data
@Component
public abstract class BaseProcessor<R> {

    @Autowired
    protected RestTemplate restTemplate;

    @Autowired
    protected RestHighLevelClient elasticClient;

    protected final Gson GSON = new Gson();

    protected HttpHeaders jsonHeaders = new HttpHeaders();

    @PostConstruct
    private void init() {
        jsonHeaders.setContentType(APPLICATION_JSON);
    }

    abstract R process();
}
