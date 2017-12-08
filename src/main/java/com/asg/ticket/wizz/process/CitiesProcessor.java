package com.asg.ticket.wizz.process;

import com.asg.ticket.wizz.dto.city.Cities;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

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
        return cities;
    }
}
