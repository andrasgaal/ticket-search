package com.asg.ticket.wizz.dto.search.response;

import lombok.Data;

@Data
public class Price {

    private final double amount;
    private final String currencyCode;

    public Price(int amount, String currencyCode) {
        this.amount = amount;
        this.currencyCode = currencyCode;
    }
}
