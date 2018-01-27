package com.asg.ticket.wizz.dto.search.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RequestFlight {

    private final String departureStation;
    private final String arrivalStation;
    private final String departureDate;
}
