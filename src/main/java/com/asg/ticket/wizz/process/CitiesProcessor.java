package com.asg.ticket.wizz.process;

import com.asg.ticket.wizz.dto.city.Cities;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.index.IndexRequest;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;

import static java.util.UUID.randomUUID;
import static org.springframework.http.HttpMethod.GET;

@Slf4j
@Component
public class CitiesProcessor extends BaseProcessor<Cities> {

    private static final String CITIES_PATH = "/asset/map";
    private String metadataUrl;

    public void setMetadataUrl(String metadataUrl) {
        this.metadataUrl = metadataUrl;
    }

    @Override
    public Cities process() {
        String citiesUrl = UriComponentsBuilder.fromHttpUrl(metadataUrl)
                .path(CITIES_PATH)
                .queryParam("languageCode", "en-gb").toUriString();
        HttpEntity<String> entity = new HttpEntity<>(jsonHeaders);

        log.info("Fetching cities...");
        ResponseEntity<String> response = restTemplate.exchange(citiesUrl, GET, entity, String.class);
        Cities cities = GSON.fromJson(response.getBody(), Cities.class);
        log.info("Received cities={}", cities);

        reportCities(cities);
        return cities;
    }

    private void reportCities(Cities cities) {
        try {
            HashMap<String, Object> source = new HashMap<>();
            source.put("searchDate", new Date());
            source.put("cities", cities.getCities());
            source.put("citiesCount", cities.getCities().length);
            elasticClient.index(new IndexRequest("cities", "cities", randomUUID().toString()).source(source));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
