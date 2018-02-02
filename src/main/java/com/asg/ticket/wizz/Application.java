package com.asg.ticket.wizz;

import com.asg.ticket.wizz.config.ClientConfiguration;
import com.asg.ticket.wizz.fetch.BaseProcessor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableScheduling
@ComponentScan(basePackageClasses = {
        Application.class,
        ClientConfiguration.class,
        BaseProcessor.class
})
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
