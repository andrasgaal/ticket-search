package com.asg.ticket.wizz.dto.search.request;

import lombok.Data;

@Data
public class SearchRequest {

    private final Flight[] flightList;
    private final String adultCount;

    public SearchRequest(Flight[] flightList, String adultCount) {
        this.flightList = flightList;
        this.adultCount = adultCount;
    }
}
