package com.asg.ticket.wizz.process;

import com.asg.ticket.wizz.dto.Metadata;
import com.asg.ticket.wizz.dto.city.Cities;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.HashMap;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static org.springframework.http.HttpMethod.GET;

@Slf4j
@Component
public class CitiesProcessor extends BaseProcessor<Cities> {

    private static final String CITIES_PATH = "/asset/map";
    private Metadata metadata;

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public Cities process() {
        String citiesUrl = UriComponentsBuilder.fromHttpUrl(metadata.getApiUrl())
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
        HashMap<String, Object> source = new HashMap<>();
        source.put("searchDate", LocalDateTime.now().format(ISO_LOCAL_DATE_TIME));
        source.put("cities", cities.getCities());
        source.put("citiesCount", cities.getCities().length);
        elasticClient.report("cities", source);
    }
}
