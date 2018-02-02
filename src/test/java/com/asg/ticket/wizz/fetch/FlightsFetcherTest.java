package com.asg.ticket.wizz.fetch;

import com.asg.ticket.wizz.dto.Metadata;
import com.asg.ticket.wizz.dto.city.Cities;
import com.asg.ticket.wizz.dto.city.City;
import com.asg.ticket.wizz.dto.city.Connection;
import com.asg.ticket.wizz.dto.search.response.SearchResponse;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;

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

    private FlightsFetcher processor;

    @Before
    public void setUp() throws Exception {
        processor = new FlightsFetcher(1);
        processor.setElasticClient(elasticClient);
        processor.setRestTemplate(restTemplate);
        processor.setMetadata(new Metadata("http://someMetadataUrl.com"));
        processor.setAllCities(createCities());
    }

    @Test
    public void process() throws Exception {
        String content = readFile("search_7_7_1.json");

        when(restTemplate.exchange(anyString(), eq(POST), any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>(content, OK));

        List<SearchResponse> searchResult = processor.fetchFlights(metadata, cities, currencyExchangeHolder);

        assertNotNull(searchResult);
        assertThat(searchResult.isEmpty(), is(false));
        verify(restTemplate, times(4)).exchange(contains("search"), eq(POST), any(), eq(String.class));
        verify(elasticClient, times(4)).report(eq("flights"), any(String.class));
    }

    private Cities createCities() {
        Connection[] budToLtn = {new Connection(LTN_IATA)};
        Connection[] ltnToBud = {new Connection(BUD_IATA)};
        City[] cities = {
                new City(BUD_IATA, "Hungary", budToLtn),
                new City(LTN_IATA, "United Kingdom", ltnToBud)};
        return new Cities(cities);
    }

}