package com.asg.ticket.wizz.process;

import com.asg.ticket.wizz.dto.city.Cities;
import com.asg.ticket.wizz.dto.search.response.SearchResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Slf4j
@Component
public class ProcessorStarter {

    @Autowired
    MetadataProcessor metadataProcessor;

    @Autowired
    CitiesProcessor citiesProcessor;

    @Autowired
    SearchResultProcessor searchResultProcessor;

    @Scheduled(fixedRate = 60000)
    public void startProcessors() {
        String metadataUrl = metadataProcessor.process();

        citiesProcessor.setMetadataUrl(metadataUrl);
        Cities cities = citiesProcessor.process();

        searchResultProcessor.setMetadataUrl(metadataUrl);
        searchResultProcessor.setAllCities(cities);
        List<SearchResponse> searchResponses = searchResultProcessor.process();
        log.info("Received search responses={}", searchResponses);
    }
}
