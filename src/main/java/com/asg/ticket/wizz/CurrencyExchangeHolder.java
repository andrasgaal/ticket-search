package com.asg.ticket.wizz;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Map;

@Data
@AllArgsConstructor
public class CurrencyExchangeHolder {

    private final String base;
    private final String date;
    private final Map<String, Double> rates;
}
