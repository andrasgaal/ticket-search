package com.asg.ticket.wizz.fetch;

import com.asg.ticket.wizz.CurrencyExchangeHolder;
import com.asg.ticket.wizz.config.IataWhitelist;
import com.asg.ticket.wizz.dto.Metadata;
import com.asg.ticket.wizz.dto.city.Cities;
import com.asg.ticket.wizz.dto.city.City;
import com.asg.ticket.wizz.dto.city.Connection;
import com.asg.ticket.wizz.dto.search.response.SearchResponse;
import com.asg.ticket.wizz.repository.FlightRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.contains;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpStatus.OK;

public class FlightsFetcherTest extends ProcessorTestBase {

    private static final String BUD_IATA = "BUD";
    private static final String LTN_IATA = "LTN";
    private static final String LCA_IATA = "LCA";

    private FlightsFetcher flightsFetcher;

    private FlightRepository dao;
    private IataWhitelist whitelist;
    private String content;

    @Before
    public void setUp() throws Exception {
        dao = mock(FlightRepository.class);
        whitelist = new IataWhitelist();
        flightsFetcher = new FlightsFetcher(dao, whitelist, 1, 1);
        flightsFetcher.setElasticClient(elasticClient);
        flightsFetcher.setRestTemplate(restTemplate);
        content = readFile("search_7_7_1.json");
    }

    @Test
    public void fetchWithoutWhitelist() throws Exception {
        when(restTemplate.exchange(anyString(), eq(POST), any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>(content, OK));

        List<SearchResponse> searchResult = flightsFetcher.fetchFlights(new Metadata("http://someMetadataUrl.com"),
                createCities(), createCurrencyExchange());

        assertNotNull(searchResult);
        assertThat(searchResult.isEmpty(), is(false));
        verify(restTemplate, times(4)).exchange(contains("search"), eq(POST), any(), eq(String.class));
    }

    @Test
    public void fetchWithCityWhitelist() throws Exception {
        whitelist.getCities().setEnabled(true);
        whitelist.getCities().getIatas().add(BUD_IATA);

        when(restTemplate.exchange(anyString(), eq(POST), any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>(content, OK));

        List<SearchResponse> searchResult = flightsFetcher.fetchFlights(new Metadata("http://someMetadataUrl.com"),
                createCities(), createCurrencyExchange());

        assertNotNull(searchResult);
        assertThat(searchResult.isEmpty(), is(false));
        verify(restTemplate, times(2)).exchange(contains("search"), eq(POST), any(), eq(String.class));
    }
    @Test
    public void fetchWithConnectionWhitelist() throws Exception {
        whitelist.getConnections().setEnabled(true);
        whitelist.getConnections().getIatas().add(LTN_IATA);

        when(restTemplate.exchange(anyString(), eq(POST), any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>(content, OK));

        List<SearchResponse> searchResult = flightsFetcher.fetchFlights(new Metadata("http://someMetadataUrl.com"),
                createCities(), createCurrencyExchange());

        assertNotNull(searchResult);
        assertThat(searchResult.isEmpty(), is(false));
        verify(restTemplate, times(1)).exchange(contains("search"), eq(POST), any(), eq(String.class));
    }

    private Cities createCities() {
        Connection[] connectionFromBud = {new Connection(LTN_IATA), new Connection(LCA_IATA)};
        Connection[] connectionFromLtn = {new Connection(BUD_IATA), new Connection(LCA_IATA)};
        City[] cities = {
                new City(BUD_IATA, "Hungary", connectionFromBud),
                new City(LTN_IATA, "United Kingdom", connectionFromLtn)};
        return new Cities(cities);
    }

    private CurrencyExchangeHolder createCurrencyExchange() {
        Map<String, Double> rates = new HashMap<>();
        rates.put("GBP", 0.0025);
        return new CurrencyExchangeHolder("HUF", "someDate", rates);
    }

}