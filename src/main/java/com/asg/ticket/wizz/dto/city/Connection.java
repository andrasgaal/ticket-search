package com.asg.ticket.wizz.dto.city;

import lombok.Data;

@Data
public class Connection {

    private final String iata;

    public Connection(String iata) {
        this.iata = iata;
    }
}
