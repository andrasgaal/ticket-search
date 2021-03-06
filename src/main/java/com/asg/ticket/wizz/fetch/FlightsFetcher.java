package com.asg.ticket.wizz.fetch;

import com.asg.ticket.wizz.CurrencyExchangeHolder;
import com.asg.ticket.wizz.CurrencyExchangeUtil.CurrencyExchangeException;
import com.asg.ticket.wizz.config.IataWhitelist;
import com.asg.ticket.wizz.dto.Flight;
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
import com.asg.ticket.wizz.repository.FlightRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.asg.ticket.wizz.CurrencyExchangeUtil.exchangeToHuf;
import static java.lang.String.valueOf;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static java.util.Arrays.stream;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static java.util.concurrent.Executors.newFixedThreadPool;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpMethod.POST;

@Slf4j
@Component
public class FlightsFetcher extends BaseProcessor<List<SearchResponse>> {

    private static final String SEARCH_PATH = "/search/search";

    private final List<String> searchDates;
    private ExecutorService executor;
    private String searchUrl;
    private int searchDays;

    private FlightRepository flightRepository;
    private IataWhitelist iataWhitelist;

    public FlightsFetcher(
            @Autowired FlightRepository flightRepository,
            @Autowired IataWhitelist iataWhitelist,
            @Value("${search.days}") int searchDays,
            @Value("${search.threads}") int searchThreadCount) {
        this.flightRepository = flightRepository;
        this.iataWhitelist = iataWhitelist;
        this.searchDays = searchDays;
        this.searchDates = getDates();
        this.executor = searchThreadCount == 0 ? newCachedThreadPool() : newFixedThreadPool(searchThreadCount);
    }

    public List<SearchResponse> fetchFlights(Metadata metadata, Cities allCities, CurrencyExchangeHolder currencyExchangeHolder) {
        searchUrl = UriComponentsBuilder.fromHttpUrl(metadata.getApiUrl()).path(SEARCH_PATH).toUriString();

        List<Future<Optional<SearchResponse>>> futureResponses = stream(allCities.getCities())
                .filter(cityInWhitelistIfEnabled())
                .flatMap(this::futureSearchResponsesForCity)
                .collect(toList());

        List<SearchResponse> searchResponses = collectSearchResponses(futureResponses);
        reportSearchResponses(searchResponses, currencyExchangeHolder);
        return searchResponses;
    }

    private Stream<? extends Future<Optional<SearchResponse>>> futureSearchResponsesForCity(City city) {
        return stream(city.getConnections())
                .filter(connectionInWhitelistIfEnabled())
                .flatMap(connection -> futureSearchResponseForRoute(city, connection));
    }

    private Stream<Future<Optional<SearchResponse>>> futureSearchResponseForRoute(City city, Connection connection) {
        return searchDates.stream()
                .map(date -> executor.submit(() -> futureSearchResponseForFlight(city.getIata(), connection.getIata(), date)));
    }

    private Predicate<City> cityInWhitelistIfEnabled() {
        return city -> !iataWhitelist.getCities().isEnabled() || iataWhitelist.getCities().getIatas().contains(city.getIata());
    }

    private Predicate<Connection> connectionInWhitelistIfEnabled() {
        return connection -> !iataWhitelist.getConnections().isEnabled() || iataWhitelist.getConnections().getIatas().contains(connection.getIata());
    }

    private Optional<SearchResponse> futureSearchResponseForFlight(String departureStation, String arrivalStation, String departureDate) throws IOException {
        RequestFlight[] requestFlights = {
                new RequestFlight(departureStation, arrivalStation, departureDate)};

        String postBody = mapper.writeValueAsString(new SearchRequest(requestFlights, valueOf(1)));
        HttpEntity<String> entity = new HttpEntity<>(postBody, jsonHeaders);

        log.info("Executing search for departureStation={}, arrivalStation={}, date={}",
                departureStation, arrivalStation, departureDate);
        try {
            ResponseEntity<String> response = restTemplate.exchange(searchUrl, POST, entity, String.class);
            SearchResponse searchResponse = mapper.readValue(response.getBody(), SearchResponse.class);
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

    private void reportSearchResponses(List<SearchResponse> searchResponses, CurrencyExchangeHolder currencyExchangeHolder) {
        searchResponses.forEach(searchResponse -> {
            ResponseFlight[] outboundFlights = searchResponse.getOutboundFlights() == null ? new ResponseFlight[0] : searchResponse.getOutboundFlights();
            stream(outboundFlights)
                    .forEach(
                            responseFlight -> persistFlight(responseFlight, currencyExchangeHolder)
                    );
        });
    }

    private void persistFlight(ResponseFlight responseFlight, CurrencyExchangeHolder currencyExchangeHolder) {
        Optional<Fare> basicFare = stream(responseFlight.getFares()).filter(fare -> fare.getBundle().equals("BASIC")).findFirst();
        if (basicFare.isPresent()) {
            Price basePriceInLocalCurrency = basicFare.get().getBasePrice();
            try {
                LocalDateTime searchDateTime = LocalDateTime.now();
                LocalDateTime flightDateTime = LocalDateTime.parse(responseFlight.getDepartureDateTime());
                String departureStation = responseFlight.getDepartureStation();
                String arrivalStation = responseFlight.getArrivalStation();
                double priceInHuf = exchangeToHuf(currencyExchangeHolder, basePriceInLocalCurrency);
                Flight flight = new Flight(UUID.randomUUID().toString(), searchDateTime, flightDateTime, departureStation, arrivalStation, priceInHuf);

                flightRepository.save(flight);
                log.info("Flight persisted, {}", flight);
            } catch (CurrencyExchangeException e) {
                log.error(e.getMessage());
            }
        } else {
            log.error("BASIC bundle not found for flight={}", responseFlight);
        }
    }

    private Consumer<ResponseFlight> reportFlight(CurrencyExchangeHolder currencyExchangeHolder) {
        return responseFlight -> {
            Map<String, Object> source = new HashMap<>();
            source.put("searchDate", LocalDateTime.now().format(ISO_LOCAL_DATE_TIME));
            source.put("flightDate", responseFlight.getDepartureDateTime());
            source.put("departureStation", responseFlight.getDepartureStation());
            source.put("arrivalStation", responseFlight.getArrivalStation());
            Optional<Fare> basicFare = stream(responseFlight.getFares()).filter(fare -> fare.getBundle().equals("BASIC")).findFirst();
            if (basicFare.isPresent()) {
                Price basePriceInLocalCurrency = basicFare.get().getBasePrice();
                try {
                    source.put("price", exchangeToHuf(currencyExchangeHolder, basePriceInLocalCurrency));
                    elasticClient.report("flights", source);
                } catch (CurrencyExchangeException e) {
                    log.error(e.getMessage());
                }
            } else {
                log.debug("BASIC bundle not found for flight={}", responseFlight);
            }
        };
    }
}
