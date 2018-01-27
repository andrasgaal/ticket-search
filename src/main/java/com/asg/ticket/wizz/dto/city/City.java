package com.asg.ticket.wizz.dto.city;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class City {

    private String iata;
    private String countryName;
    private Connection[] connections;
}
