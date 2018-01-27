package com.asg.ticket.wizz.dto.search.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchResponse {

    private ResponseFlight[] outboundFlights;
    private ResponseFlight[] returnFlights;
    private String currencyCode;
}
