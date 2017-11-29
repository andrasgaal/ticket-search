package com.asg.ticket.wizz.process;

import com.asg.ticket.wizz.dto.search.request.Flight;
import com.asg.ticket.wizz.dto.search.request.SearchRequestBody;
import com.asg.ticket.wizz.dto.search.response.SearchResponseBody;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static java.lang.String.valueOf;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.util.concurrent.Executors.newCachedThreadPool;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component("searchResultProcessor")
public class SearchResultProcessor implements Processor<String> {

    private static final String SEARCH_PATH = "/search/search";
    private static final int HALF_YEAR_IN_DAYS = 180;

    private final RestTemplate restClient;
    private final Gson GSON = new Gson();
    private final ExecutorService executor = newCachedThreadPool();

    private String searchUrl;
    private HttpHeaders headers = new HttpHeaders();

    @Autowired
    public SearchResultProcessor(RestTemplate restClient) {
        this.restClient = restClient;
        headers.setContentType(APPLICATION_JSON);
    }


    @Override
    public void process(String input) {
        searchUrl = UriComponentsBuilder.fromHttpUrl(input).path(SEARCH_PATH).toUriString();

        List<Future<SearchResponseBody>> searchRequests = new ArrayList<>();
        getDatesForHalfAYear().forEach(date -> {
            Future<SearchResponseBody> searchRequest =
                    executor.submit(() -> getSearchResponse("BUD", "LTN", date));
            executor.submit(() -> getSearchResponse("LTN", "BUD", date));
            searchRequests.add(searchRequest);
        });

        List<SearchResponseBody> searchResult = getSearchResponses(searchRequests);
    }

    private SearchResponseBody getSearchResponse(String departureStation, String arrivalStation, String departureDate) {
        Flight[] flights = {
                new Flight(departureStation, arrivalStation, departureDate)};

        String postBody = GSON.toJson(new SearchRequestBody(flights, valueOf(1)));
        HttpEntity<String> entity = new HttpEntity<>(postBody, headers);

        ResponseEntity<String> response = restClient.exchange(searchUrl, POST, entity, String.class);
        System.out.println(response.getBody());
        return new Gson().fromJson(response.getBody(), SearchResponseBody.class);
    }

    public List<String> getDatesForHalfAYear() {
        ArrayList<String> dates = new ArrayList<>();
        for (int daysFromNow = 0; daysFromNow < HALF_YEAR_IN_DAYS; daysFromNow++) {
            dates.add(LocalDate.now().plusDays(daysFromNow).format(ISO_LOCAL_DATE));
        }

        return dates;
    }

    private List<SearchResponseBody> getSearchResponses(List<Future<SearchResponseBody>> searchRequests) {
        return searchRequests
                .stream()
                .map(result -> {
                    try {
                        return result.get();
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
