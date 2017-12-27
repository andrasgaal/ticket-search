package com.asg.ticket.wizz.process;

import com.google.gson.Gson;
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Matchers;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileReader;
import java.io.ObjectInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.notNull;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.springframework.http.HttpStatus.OK;

public class MetadataProcessorTest {

    @InjectMocks
    private RestTemplate restTemplate;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
    }

    @Test
    public void process() throws Exception {
        Path path = Paths.get(getClass().getClassLoader().getResource("metadata_7.7.1.txt").getPath());

        File metadataFile = new File(getClass().getClassLoader().getResource("metadata_7.7.1.txt").getFile());
        FileReader metadataReader = new FileReader(metadataFile);

        StringBuilder content = new StringBuilder();
        for (String line : Files.readAllLines(path)){
            content.append(line);
        }

        when(restTemplate.exchange(any(), any(), any(), eq(String.class))).thenReturn(new ResponseEntity<String>(content.toString(), OK));

        MetadataProcessor processor = new MetadataProcessor();
        String metadata = processor.process();

        assertThat(metadata, is(Matchers.notNull()));

    }

}