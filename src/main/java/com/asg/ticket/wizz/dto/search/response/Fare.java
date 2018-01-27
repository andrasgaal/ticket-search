package com.asg.ticket.wizz.dto.search.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Fare {

    private String bundle;
    private Price basePrice;
    private Price discountedPrice;
    private Price fullBasePrice;
    private Price discountedFarePrice;
}
