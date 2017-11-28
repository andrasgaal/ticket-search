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

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static java.lang.String.valueOf;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@Component("searchResultProcessor")
public class SearchResultProcessor implements Processor<String> {

    private static final String SEARCH_PATH = "/search/search";
    private static final int HALF_YEAR_IN_DAYS = 180;
    private final DateTimeFormatter searchDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-DD");

    private final RestTemplate restClient;
    private final Gson GSON = new Gson();
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

        List<String> datesForHalfAYear = getDatesForHalfAYear();
        getSearchResponse("BUD", "LTN", "2017-12-01");
    }

    private void getSearchResponse(String departureStation, String arrivalStation, String departureDate) {
        Flight[] flights = {
                new Flight(departureStation, arrivalStation, departureDate)};

        String postBody = GSON.toJson(new SearchRequestBody(flights, valueOf(1)));
        HttpEntity<String> entity = new HttpEntity<>(postBody, headers);


        ResponseEntity<String> response = restClient.exchange(searchUrl, POST, entity, String.class);
        System.out.println(response.getBody());
        SearchResponseBody searchResponseBody = new Gson().fromJson(response.getBody(), SearchResponseBody.class);
    }

    public List<String> getDatesForHalfAYear() {
        ArrayList<String> dates = new ArrayList<>();
        for (int daysFromNow = 0; daysFromNow < HALF_YEAR_IN_DAYS; daysFromNow++){
            dates.add(LocalDate.now().plusDays(daysFromNow).format(searchDateFormat));
        }

        return dates;
    }
}
