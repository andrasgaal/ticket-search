package com.asg.ticket.wizz.process;

import com.asg.ticket.wizz.dto.Cities;
import com.asg.ticket.wizz.dto.search.request.Flight;
import com.asg.ticket.wizz.dto.search.request.SearchRequest;
import com.asg.ticket.wizz.dto.search.response.SearchResponse;
import com.google.gson.Gson;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
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

@Component
public class SearchResultProcessor extends BaseProcessor implements Processor<List<SearchResponse>> {

    private static final String SEARCH_PATH = "/search/search";
    private static final int HALF_YEAR_IN_DAYS = 180;

    private final Gson GSON = new Gson();
    private final ExecutorService executor = newCachedThreadPool();
    private String metadataUrl;
    private Cities cities;
    private String searchUrl;

    public void setMetadataUrl(String metadataUrl) {
        this.metadataUrl = metadataUrl;
    }

    public void setCities(Cities cities) {
        this.cities = cities;
    }

    @Override
    public List<SearchResponse> process() {
        searchUrl = UriComponentsBuilder.fromHttpUrl(metadataUrl).path(SEARCH_PATH).toUriString();

        List<Future<SearchResponse>> searchRequests = new ArrayList<>();
        getDatesForHalfAYear().forEach(date -> {
            Future<SearchResponse> outbound =
                    executor.submit(() -> getSearchResponse("BUD", "LTN", date));
            Future<SearchResponse> inbound =
                    executor.submit(() -> getSearchResponse("LTN", "BUD", date));
            searchRequests.add(outbound);
            searchRequests.add(inbound);
        });

        return getSearchResponses(searchRequests);
    }

    private SearchResponse getSearchResponse(String departureStation, String arrivalStation, String departureDate) {
        Flight[] flights = {
                new Flight(departureStation, arrivalStation, departureDate)};

        String postBody = GSON.toJson(new SearchRequest(flights, valueOf(1)));
        HttpEntity<String> entity = new HttpEntity<>(postBody, jsonHeaders);

        ResponseEntity<String> response = restTemplate.exchange(searchUrl, POST, entity, String.class);
        return new Gson().fromJson(response.getBody(), SearchResponse.class);
    }

    public List<String> getDatesForHalfAYear() {
        ArrayList<String> dates = new ArrayList<>();
        //for (int daysFromNow = 0; daysFromNow < HALF_YEAR_IN_DAYS; daysFromNow++) {
        for (int daysFromNow = 0; daysFromNow < 10; daysFromNow++) {
            dates.add(LocalDate.now().plusDays(daysFromNow).format(ISO_LOCAL_DATE));
        }

        return dates;
    }

    private List<SearchResponse> getSearchResponses(List<Future<SearchResponse>> searchRequests) {
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
