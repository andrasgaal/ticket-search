package com.asg.ticket.wizz.process;

import com.asg.ticket.wizz.dto.Cities;
import com.asg.ticket.wizz.dto.Metadata;
import com.google.gson.Gson;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import static org.springframework.http.HttpMethod.GET;

@Component
public class CitiesProcessor extends BaseProcessor implements Processor<Cities> {

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

        ResponseEntity<String> response = restTemplate.exchange(citiesUrl, GET, entity, String.class);
        return new Gson().fromJson(response.getBody(), Cities.class);
    }
}
