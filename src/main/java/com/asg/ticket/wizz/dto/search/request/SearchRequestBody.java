package com.asg.ticket.wizz.dto.search.request;

public class SearchRequestBody {

    private final Flight[] flightList;
    private final String adultCount;

    public SearchRequestBody(Flight[] flightList, String adultCount) {
        this.flightList = flightList;
        this.adultCount = adultCount;
    }

    public Flight[] getFlightList() {
        return flightList;
    }

    public String getAdultCount() {
        return adultCount;
    }
}
