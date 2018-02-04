package com.asg.ticket.wizz.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "search.whitelist")
public class IataWhitelist {

    private List<String> iatas = new ArrayList<>();

    public List<String> getIatas() {
        return iatas;
    }
}