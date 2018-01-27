package com.asg.ticket.wizz.dto.search.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Price {

    private double amount;
    private String currencyCode;
}
