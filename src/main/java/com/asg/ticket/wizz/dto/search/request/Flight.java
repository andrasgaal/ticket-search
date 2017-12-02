package com.asg.ticket.wizz.dto.search.request;

import lombok.Data;

@Data
public class Flight {

    private final String departureStation;
    private final String arrivalStation;
    private final String departureDate;

    public Flight(String departureStation, String arrivalStation, String departureDate) {
        this.departureStation = departureStation;
        this.arrivalStation = arrivalStation;
        this.departureDate = departureDate;
    }
}
