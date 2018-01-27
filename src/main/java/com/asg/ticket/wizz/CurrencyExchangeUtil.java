package com.asg.ticket.wizz;

import com.asg.ticket.wizz.dto.search.response.Price;

import java.util.Map;

public class CurrencyExchangeUtil {

    public static double exchangeToHuf(CurrencyExchangeHolder currencyExchangeHolder, Price priceInLocalCurrency) {

        double amount = priceInLocalCurrency.getAmount();
        String currency = priceInLocalCurrency.getCurrencyCode();
        if (currency.equals("HUF")) {
            return amount;
        }
        Map<String, Double> rates = currencyExchangeHolder.getRates();
        Double localToHufRate = rates.get(currency);
        if (localToHufRate != null) {
            return (1 / localToHufRate) * amount;
        }
        throw new RuntimeException("Currency not found, " + currency);
    }
}
