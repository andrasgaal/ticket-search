package com.asg.ticket.wizz.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "search.whitelist")
public class IataWhitelist {

    private final Cities cities = new Cities();

    private final Connections connections = new Connections();

    public Cities getCities() {
        return cities;
    }

    public Connections getConnections() {
        return connections;
    }


    public static class Cities {

        private boolean enabled;

        private List<String> iatas = new ArrayList<>();

        public boolean isEnabled() {
            return enabled;
        }

        public List<String> getIatas() {
            return iatas;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

    public static class Connections {

        private boolean enabled;

        private List<String> iatas = new ArrayList<>();

        public boolean isEnabled() {
            return enabled;
        }

        public List<String> getIatas() {
            return iatas;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
    }

}
