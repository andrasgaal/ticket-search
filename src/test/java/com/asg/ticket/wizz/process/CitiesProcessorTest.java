package com.asg.ticket.wizz.process;

import com.asg.ticket.wizz.dto.Metadata;
import com.asg.ticket.wizz.dto.city.Cities;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.hamcrest.Matchers.greaterThan;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;

public class CitiesProcessorTest extends ProcessorTestBase {

    private CitiesProcessor processor;

    @Before
    public void setUp() throws Exception {
        processor = new CitiesProcessor();
        processor.setElasticClient(elasticClient);
        processor.setRestTemplate(restTemplate);
        processor.setMetadata(new Metadata("http://someMetadataUrl.com"));
    }

    @Test
    public void process() throws Exception {
        String content = readFile("cities_7_7_1.json");

        when(restTemplate.exchange(anyString(), eq(GET), any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>(content, OK));

        Cities cities = processor.process();

        assertNotNull(cities.getCities());
        assertThat(cities.getCities().length, greaterThan(5));
        verify(restTemplate).exchange(contains("asset/map"), eq(GET), any(), eq(String.class));
        verify(elasticClient).report(eq("cities"), any(Map.class));
    }

}