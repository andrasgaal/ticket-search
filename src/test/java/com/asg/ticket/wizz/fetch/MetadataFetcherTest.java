package com.asg.ticket.wizz.fetch;

import com.asg.ticket.wizz.dto.Metadata;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpStatus.OK;


@RunWith(MockitoJUnitRunner.class)
public class MetadataFetcherTest extends ProcessorTestBase {

    private MetadataFetcher processor;

    @Before
    public void setUp() throws Exception {
        processor = new MetadataFetcher();
        processor.setElasticClient(elasticClient);
        processor.setRestTemplate(restTemplate);
    }

    @Test
    public void process() throws Exception {
        String content = readFile("metadata_7_7_1.json");

        when(restTemplate.exchange(anyString(), eq(GET), any(), eq(String.class)))
                .thenReturn(new ResponseEntity<>(content, OK));

        Metadata metadata = processor.fetchMetadata();

        assertThat(metadata.getApiUrl(), is("https://be.wizzair.com/7.7.1/Api"));
        verify(restTemplate).exchange(contains("metadata"), eq(GET), any(), eq(String.class));
        verify(elasticClient).report(eq("metadata"), any(Map.class));
    }

}