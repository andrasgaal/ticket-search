package com.asg.ticket.wizz.process;

import com.asg.ticket.wizz.WhitelistConfig;
import com.asg.ticket.wizz.dto.Metadata;
import com.asg.ticket.wizz.dto.city.Cities;
import com.asg.ticket.wizz.dto.city.City;
import com.asg.ticket.wizz.dto.city.Connection;
import com.asg.ticket.wizz.dto.search.request.Flight;
import com.asg.ticket.wizz.dto.search.request.SearchRequest;
import com.asg.ticket.wizz.dto.search.response.SearchResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Predicate;

import static java.lang.String.valueOf;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.util.Arrays.stream;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpMethod.POST;

@Slf4j
@Component
public class SearchResultProcessor extends BaseProcessor<List<SearchResponse>> {

    private static final String SEARCH_PATH = "/search/search";

    private final ExecutorService executor = newCachedThreadPool();
    private final List<String> searchDates;
    private Metadata metadata;
    private Cities allCities;
    private String searchUrl;
    private int searchDays;

    @Autowired
    private WhitelistConfig whitelistConfig;

    @Value("${search.whitelist.enabled}")
    private boolean whiteListEnabled;

    public SearchResultProcessor(@Value("${search.days}") int searchDays) {
        this.searchDays = searchDays;
        this.searchDates = getDates();
    }

    public void setMetadata(Metadata metadataUrl) {
        this.metadata = metadataUrl;
    }

    public void setAllCities(Cities allCities) {
        this.allCities = allCities;
    }

    @Override
    public List<SearchResponse> process() {
        searchUrl = UriComponentsBuilder.fromHttpUrl(metadata.getApiUrl()).path(SEARCH_PATH).toUriString();

        List<Future<SearchResponse>> futureResponses = new ArrayList<>();
        stream(allCities.getCities())
                .filter(cityNotInWhitelistIfEnabled())
                .forEach(city -> {
                    Connection[] connections = city.getConnections();
                    stream(connections).filter(connectionNotInWhitelistIfEnabled()).forEach(connection -> searchDates.forEach(date -> {
                        futureResponses.add(executor.submit(() -> getSearchResponse(city.getIata(), connection.getIata(), date)));
                        futureResponses.add(executor.submit(() -> getSearchResponse(connection.getIata(), city.getIata(), date)));
                    }));
                });

        List<SearchResponse> searchResponses = collectSearchResponses(futureResponses);
        reportFlights(searchResponses);
        return searchResponses;
    }

    private Predicate<City> cityNotInWhitelistIfEnabled() {
        return city -> (whiteListEnabled && inIataWhiteList(city.getIata())) || !whiteListEnabled;
    }

    private Predicate<Connection> connectionNotInWhitelistIfEnabled() {
        return connection -> (whiteListEnabled && inIataWhiteList(connection.getIata())) || !whiteListEnabled;
    }

    private boolean inIataWhiteList(String iata) {
        return whitelistConfig.getIatas().contains(iata);
    }

    private SearchResponse getSearchResponse(String departureStation, String arrivalStation, String departureDate) {
        Flight[] flights = {
                new Flight(departureStation, arrivalStation, departureDate)};

        String postBody = GSON.toJson(new SearchRequest(flights, valueOf(1)));
        HttpEntity<String> entity = new HttpEntity<>(postBody, jsonHeaders);

        log.info("Executing search for departureStation={}, arrivalStation={}, date={}",
                departureStation, arrivalStation, departureDate);
        ResponseEntity<String> response = restTemplate.exchange(searchUrl, POST, entity, String.class);
        return GSON.fromJson(response.getBody(), SearchResponse.class);
    }

    private List<String> getDates() {
        ArrayList<String> dates = new ArrayList<>();
        for (int daysFromNow = 0; daysFromNow < searchDays; daysFromNow++) {
            dates.add(LocalDate.now().plusDays(daysFromNow).format(ISO_LOCAL_DATE));
        }

        return dates;
    }

    private List<SearchResponse> collectSearchResponses(List<Future<SearchResponse>> futureResponses) {
        return futureResponses.stream()
                .map(this::safeFutureGet)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
    }

    private Optional<SearchResponse> safeFutureGet(Future<SearchResponse> searchResponseFuture) {
        try {
            return of(searchResponseFuture.get());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return empty();
    }

    private void reportFlights(List<SearchResponse> searchResponses) {
        searchResponses.forEach(searchResponse -> {
            elasticClient.report("flights", GSON.toJson(searchResponse));
        });
    }
}
