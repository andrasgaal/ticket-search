package com.asg.ticket.wizz.dto;

import lombok.Data;

@Data
public class Metadata {

    private final String apiUrl;

    public Metadata(String apiUrl) {
        this.apiUrl = apiUrl;
    }
}
