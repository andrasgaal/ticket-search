package com.asg.ticket.wizz.dto.search.response;

public class SearchResponseBody {

    private final Flight[] flights;
    private final Flight[] returnFlights;
    private final String currencyCode;

    public SearchResponseBody(Flight[] flights, Flight[] returnFlights, String currencyCode) {
        this.flights = flights;
        this.returnFlights = returnFlights;
        this.currencyCode = currencyCode;
    }

    public Flight[] getFlights() {
        return flights;
    }

    public Flight[] getReturnFlights() {
        return returnFlights;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }
}
