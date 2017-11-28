package com.asg.ticket.wizz.dto.search.response;

public class Price {

    private final int amount;
    private final String currencyCode;

    public Price(int amount, String currencyCode) {
        this.amount = amount;
        this.currencyCode = currencyCode;
    }

    public int getAmount() {
        return amount;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }
}
