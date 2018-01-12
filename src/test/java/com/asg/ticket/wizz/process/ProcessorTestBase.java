package com.asg.ticket.wizz.process;

import com.asg.ticket.wizz.ElasticClient;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.mockito.Mock;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.mockito.MockitoAnnotations.initMocks;

public class ProcessorTestBase {

    @Mock
    protected RestTemplate restTemplate;

    @Mock
    protected ElasticClient elasticClient;

    @Before
    public void baseSeUp() throws Exception {
        initMocks(this);
    }

    protected String readFile(String fileName) throws IOException {
        return IOUtils.toString(this.getClass()
                .getResourceAsStream("/" + fileName), UTF_8.name());
    }
}
