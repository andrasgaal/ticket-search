package com.asg.ticket.wizz.process;

import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

import static org.springframework.http.MediaType.APPLICATION_JSON;

@Data
@Component
public class BaseProcessor {

    @Autowired
    protected RestTemplate restTemplate;

    protected HttpHeaders jsonHeaders = new HttpHeaders();

    @PostConstruct
    private void init() {
        jsonHeaders.setContentType(APPLICATION_JSON);
    }
}
