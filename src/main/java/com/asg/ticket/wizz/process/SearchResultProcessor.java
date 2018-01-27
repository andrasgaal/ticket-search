package com.asg.ticket.wizz.process;

import com.asg.ticket.wizz.CurrencyExchangeHolder;
import com.asg.ticket.wizz.config.WhitelistConfig;
import com.asg.ticket.wizz.dto.Metadata;
import com.asg.ticket.wizz.dto.city.Cities;
import com.asg.ticket.wizz.dto.city.City;
import com.asg.ticket.wizz.dto.city.Connection;
import com.asg.ticket.wizz.dto.search.request.RequestFlight;
import com.asg.ticket.wizz.dto.search.request.SearchRequest;
import com.asg.ticket.wizz.dto.search.response.Fare;
import com.asg.ticket.wizz.dto.search.response.Price;
import com.asg.ticket.wizz.dto.search.response.ResponseFlight;
import com.asg.ticket.wizz.dto.search.response.SearchResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.asg.ticket.wizz.CurrencyExchangeUtil.exchangeToHuf;
import static java.lang.String.valueOf;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
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
    private CurrencyExchangeHolder currencyExchangeHolder;
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

    public void seCurrencyExchange(CurrencyExchangeHolder currencyExchangeHolder) {
        this.currencyExchangeHolder = currencyExchangeHolder;
    }

    private Predicate<Connection> connectionInWhitelistIfEnabled() {
        return connection -> (whiteListEnabled && inIataWhiteList(connection.getIata())) || !whiteListEnabled;
    }

    @Override
    public List<SearchResponse> process() {
        searchUrl = UriComponentsBuilder.fromHttpUrl(metadata.getApiUrl()).path(SEARCH_PATH).toUriString();

        List<Future<Optional<SearchResponse>>> futureResponses = new ArrayList<>();
        stream(allCities.getCities())
                .filter(cityInWhitelistIfEnabled())
                .forEach(city -> {
                    Connection[] connections = city.getConnections();
                    stream(connections).filter(connectionInWhitelistIfEnabled()).forEach(connection -> searchDates.forEach(date -> {
                        futureResponses.add(executor.submit(() -> getSearchResponseFor(city.getIata(), connection.getIata(), date)));
                        futureResponses.add(executor.submit(() -> getSearchResponseFor(connection.getIata(), city.getIata(), date)));
                    }));
                });

        List<SearchResponse> searchResponses = collectSearchResponses(futureResponses);
        reportSearchResponses(searchResponses);
        return searchResponses;
    }

    private Predicate<City> cityInWhitelistIfEnabled() {
        return city -> (whiteListEnabled && inIataWhiteList(city.getIata())) || !whiteListEnabled;
    }

    private boolean inIataWhiteList(String iata) {
        return whitelistConfig.getIatas().contains(iata);
    }

    private Optional<SearchResponse> getSearchResponseFor(String departureStation, String arrivalStation, String departureDate) {
        RequestFlight[] requestFlights = {
                new RequestFlight(departureStation, arrivalStation, departureDate)};

        String postBody = GSON.toJson(new SearchRequest(requestFlights, valueOf(1)));
        HttpEntity<String> entity = new HttpEntity<>(postBody, jsonHeaders);

        log.info("Executing search for departureStation={}, arrivalStation={}, date={}",
                departureStation, arrivalStation, departureDate);
        try {
            ResponseEntity<String> response = restTemplate.exchange(searchUrl, POST, entity, String.class);
            SearchResponse searchResponse = GSON.fromJson(response.getBody(), SearchResponse.class);
            log.info("Received search responses={}", searchResponse);
            return of(searchResponse);
        } catch (RestClientException e) {
            log.error("Unable to get search for departureStation={}, arrivalStation={}, date={}, cause={}",
                    departureStation, arrivalStation, departureDate, e.getMessage());
        }
        return empty();
    }

    private List<String> getDates() {
        ArrayList<String> dates = new ArrayList<>();
        for (int daysFromNow = 0; daysFromNow < searchDays; daysFromNow++) {
            dates.add(LocalDate.now().plusDays(daysFromNow).format(ISO_LOCAL_DATE));
        }

        return dates;
    }

    private List<SearchResponse> collectSearchResponses(List<Future<Optional<SearchResponse>>> futureResponses) {
        return futureResponses.stream()
                .map(this::safeFutureGet)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toList());
    }

    private Optional<SearchResponse> safeFutureGet(Future<Optional<SearchResponse>> searchResponseFuture) {
        try {
            return searchResponseFuture.get();
        } catch (Exception ex) {
            log.error("Something went really bad", ex);
        }
        return empty();
    }

    private void reportSearchResponses(List<SearchResponse> searchResponses) {
        searchResponses.forEach(searchResponse -> {
            ResponseFlight[] outboundFlights = searchResponse.getOutboundFlights();
            if (outboundFlights != null && outboundFlights.length > 0) {
                stream(outboundFlights).forEach(reportFlight());
            }
            ResponseFlight[] returnFlights = searchResponse.getReturnFlights();
            if (returnFlights != null && returnFlights.length > 0) {
                stream(returnFlights).forEach(reportFlight());
            }
        });
    }

    private Consumer<ResponseFlight> reportFlight() {
        return responseFlight -> {
            Map<String, Object> source = new HashMap<>();
            source.put("searchDate", LocalDateTime.now().format(ISO_LOCAL_DATE_TIME));
            source.put("flightDate", responseFlight.getDepartureDateTime());
            source.put("departureStation", responseFlight.getDepartureStation());
            source.put("arrivalStation", responseFlight.getArrivalStation());
            Optional<Fare> basicFare = stream(responseFlight.getFares()).filter(fare -> fare.getBundle().equals("BASIC")).findFirst();
            if (basicFare.isPresent()) {
                Price basePriceInLocalCurrency = basicFare.get().getBasePrice();
                source.put("price", exchangeToHuf(currencyExchangeHolder, basePriceInLocalCurrency));
                elasticClient.report("flights", source);
            } else {
                log.error("BASIC bundle not found for flight={}", responseFlight);
            }
        };
    }
}
