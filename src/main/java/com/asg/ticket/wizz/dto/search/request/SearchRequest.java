package com.asg.ticket.wizz.dto.search.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SearchRequest {

    private final RequestFlight[] flightList;
    private final String adultCount;
}
