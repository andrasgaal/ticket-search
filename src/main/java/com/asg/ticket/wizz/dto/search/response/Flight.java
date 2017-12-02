package com.asg.ticket.wizz.dto.search.response;

import lombok.Data;

@Data
public class Flight {

    private final String departureStation;
    private final String arrivalStation;
    private final String departureDateTime;
    private final String arrivalDateTime;
    private final Fare[] fares;

    public Flight(String departureStation, String arrivalStation, String departureDateTime, String arrivalDateTime, Fare[] fares) {
        this.departureStation = departureStation;
        this.arrivalStation = arrivalStation;
        this.departureDateTime = departureDateTime;
        this.arrivalDateTime = arrivalDateTime;
        this.fares = fares;
    }
}
