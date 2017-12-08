package com.asg.ticket.wizz.dto.city;

import lombok.Data;

@Data
public class City {

    private final String iata;
    private final String countryName;
    private final Connection[] connections;

    public City(String iata, String countryName, Connection[] connections) {
        this.iata = iata;
        this.countryName = countryName;
        this.connections = connections;
    }
}
