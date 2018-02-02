package com.asg.ticket.wizz.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Flight {

    private final String Id;
    private final LocalDateTime searchDateTime;
    private final LocalDateTime flightDateTime;
    private final String departureStation;
    private final String arrivalStation;
    private final double priceInHuf;

}
