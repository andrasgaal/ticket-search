package com.asg.ticket.wizz.fetch;

import com.asg.ticket.wizz.CurrencyExchangeHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.util.UriComponentsBuilder.fromHttpUrl;

@Component
@Slf4j
public class CurrencyExchangeFetcher extends BaseProcessor<CurrencyExchangeHolder> {

    private static final String CURRENCY_URL = "https://api.fixer.io/latest";

    CurrencyExchangeHolder fetchCurrencyExchange() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        log.info("Fetching currencies...");
        ResponseEntity<String> response = restTemplate.exchange(fromHttpUrl(CURRENCY_URL).queryParam("base", "HUF").toUriString(),
                GET, entity, String.class);
        CurrencyExchangeHolder currencyExchangeHolder = GSON.fromJson(response.getBody(), CurrencyExchangeHolder.class);
        log.info("Received currencies={}", currencyExchangeHolder);
        return currencyExchangeHolder;
    }
}
