package com.asg.ticket.wizz.process;

import com.asg.ticket.wizz.dto.Cities;
import com.asg.ticket.wizz.dto.search.response.SearchResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.List;

@Component
public class ProcessorStarter {

    @Autowired
    MetadataProcessor metadataProcessor;

    @Autowired
    CitiesProcessor citiesProcessor;

    @Autowired
    SearchResultProcessor searchResultProcessor;

    @PostConstruct
    public void startProcessors() {
        String metadataUrl = metadataProcessor.process();
        System.out.println("metadataUrl = " + metadataUrl);

        citiesProcessor.setMetadataUrl(metadataUrl);
        Cities cities = citiesProcessor.process();
        System.out.println("cities = " + cities);

        searchResultProcessor.setMetadataUrl(metadataUrl);
        searchResultProcessor.setCities(cities);
        List<SearchResponse> searchResponses = searchResultProcessor.process();
        System.out.println("searchResponses = " + searchResponses);
    }
}
