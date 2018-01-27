package com.asg.ticket.wizz.dto.search.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ResponseFlight {

    private String departureStation;
    private String arrivalStation;
    private String departureDateTime;
    private String arrivalDateTime;
    private Fare[] fares;
}
