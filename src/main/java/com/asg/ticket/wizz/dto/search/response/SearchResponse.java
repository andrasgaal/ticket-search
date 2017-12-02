package com.asg.ticket.wizz.dto.search.response;

import lombok.Data;

@Data
public class SearchResponse {

    private final Flight[] outboundFlights;
    private final Flight[] returnFlights;
    private final String currencyCode;

    public SearchResponse(Flight[] outboundFlights, Flight[] returnFlights, String currencyCode) {
        this.outboundFlights = outboundFlights;
        this.returnFlights = returnFlights;
        this.currencyCode = currencyCode;
    }
}
