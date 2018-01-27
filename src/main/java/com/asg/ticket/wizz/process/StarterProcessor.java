package com.asg.ticket.wizz.process;

import com.asg.ticket.wizz.CurrencyExchangeHolder;
import com.asg.ticket.wizz.dto.Metadata;
import com.asg.ticket.wizz.dto.city.Cities;
import com.asg.ticket.wizz.dto.search.response.SearchResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class StarterProcessor {

    @Autowired
    MetadataProcessor metadataProcessor;

    @Autowired
    CitiesProcessor citiesProcessor;

    @Autowired
    SearchResultProcessor searchResultProcessor;

    @Autowired
    private CurrencyProcessor currencyProcessor;

    @Scheduled(fixedRateString = "${search.repeatInterval}")
    public void startProcessors() {
        CurrencyExchangeHolder currencyExchangeHolder = currencyProcessor.process();

        Metadata metadata = metadataProcessor.process();

        citiesProcessor.setMetadata(metadata);
        Cities cities = citiesProcessor.process();

        searchResultProcessor.setMetadata(metadata);
        searchResultProcessor.setAllCities(cities);
        searchResultProcessor.seCurrencyExchange(currencyExchangeHolder);
        List<SearchResponse> searchResponses = searchResultProcessor.process();
        log.info("Search finished", searchResponses);
    }
}
