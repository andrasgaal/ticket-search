package com.asg.ticket.wizz.fetch;

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
public class Fetcher {

    @Autowired
    private CurrencyExchangeFetcher currencyExchangeFetcher;

    @Autowired
    private MetadataFetcher metadataFetcher;

    @Autowired
    private CitiesFetcher citiesFetcher;

    @Autowired
    private FlightsFetcher flightsFetcher;

    @Scheduled(fixedRateString = "${search.repeatInterval}", initialDelayString = "${search.initialDelay}")
    public void startProcessors() {
        CurrencyExchangeHolder currencyExchangeHolder = currencyExchangeFetcher.fetchCurrencyExchange();
        Metadata metadata = metadataFetcher.fetchMetadata();
        Cities cities = citiesFetcher.fetchCities(metadata);
        List<SearchResponse> searchResponses = flightsFetcher.fetchFlights(metadata, cities, currencyExchangeHolder);

        log.info("Search finished", searchResponses);
    }
}
