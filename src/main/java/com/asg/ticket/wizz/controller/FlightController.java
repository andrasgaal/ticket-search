package com.asg.ticket.wizz.controller;

import com.asg.ticket.wizz.dto.Flight;
import com.asg.ticket.wizz.repository.FlightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

import static java.time.LocalDate.parse;
import static java.time.format.DateTimeFormatter.ISO_DATE;

@RestController
public class FlightController {

    @Autowired
    private FlightRepository flightRepository;

    @GetMapping("/getFlights")
    public List<Flight> getFlights(@RequestParam String departureStation, @RequestParam String arrivalStation, @RequestParam String flightDate) {
//        return flightRepository.getFlights(departureStation, arrivalStation, LocalDate.parse(flightDate, ISO_DATE).atStartOfDay());
        LocalDate localFlightDate = parse(flightDate, ISO_DATE);
        return flightRepository.getFlights(departureStation, arrivalStation, localFlightDate.atStartOfDay(), localFlightDate.plusDays(1).atStartOfDay());
    }
}