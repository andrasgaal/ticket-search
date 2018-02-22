package com.asg.ticket.wizz.controller;

import com.asg.ticket.wizz.dto.Flight;
import com.asg.ticket.wizz.repository.FlightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.*;

import static java.time.LocalDate.parse;
import static java.time.format.DateTimeFormatter.ISO_DATE;
import static java.util.stream.Collectors.toMap;

@RestController
public class FlightController {

    @Autowired
    private FlightRepository flightRepository;

    @GetMapping("/flights")
    public List<Flight> getFlights(@RequestParam String departureStation, @RequestParam String arrivalStation, @RequestParam String flightDate) {
        LocalDate localFlightDate = parse(flightDate, ISO_DATE);
        return flightRepository.getFlights(departureStation, arrivalStation, localFlightDate.atStartOfDay(), localFlightDate.plusDays(1).atStartOfDay());
    }

    @GetMapping("/flights/groupby/searchdate")
    public Map<LocalDate, Flight> getFlightsGroupBySearchDate(@RequestParam String departureStation, @RequestParam String arrivalStation, @RequestParam String flightDate) {
        List<Flight> flights = getFlights(departureStation, arrivalStation, flightDate);

        return flights.stream().collect(toMap(
                flght -> flght.getSearchDateTime().toLocalDate(),
                flight -> flight,
                (flight1, flight2) -> {
                    double roundernAvgPrice = Double.valueOf((flight1.getPriceInHuf() + flight2.getPriceInHuf()) / 2).intValue();
                    return new Flight(flight1.getId(), flight1.getSearchDateTime(), flight1.getFlightDateTime(), flight1.getDepartureStation(),
                            flight1.getArrivalStation(), roundernAvgPrice);
                }
        ));
    }

    @GetMapping("/iatas")
    public Set<String> getIatas() {
        return new TreeSet<>(flightRepository.getIatas());
    }
}
