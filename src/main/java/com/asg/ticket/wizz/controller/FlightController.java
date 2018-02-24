package com.asg.ticket.wizz.controller;

import com.asg.ticket.wizz.dto.Flight;
import com.asg.ticket.wizz.repository.FlightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

import static java.time.LocalDate.parse;
import static java.time.format.DateTimeFormatter.ISO_DATE;
import static java.util.Collections.reverseOrder;
import static java.util.stream.Collectors.toMap;

@RestController
@RequestMapping("/flights")
public class FlightController {

    @Autowired
    private FlightRepository flightRepository;

    @GetMapping("/iatas")
    public Set<String> getIatas() {
        return new TreeSet<>(flightRepository.getIatas());
    }

    @GetMapping("/{departureStation}/{arrivalStation}")
    public List<Flight> getFlightsForRoute(@PathVariable String departureStation, @PathVariable String arrivalStation) {
        return flightRepository.getFlightsForRoute(departureStation, arrivalStation);
    }

    @GetMapping("/{departureStation}/{arrivalStation}/{flightDate}")
    public List<Flight> getFlightsForRouteAndDate(@PathVariable String departureStation, @PathVariable String arrivalStation, @PathVariable String flightDate) {
        LocalDate localFlightDate = parse(flightDate, ISO_DATE);
        return flightRepository.getFlightsForRouteAndDate(departureStation, arrivalStation, localFlightDate.atStartOfDay(), localFlightDate.plusDays(1).atStartOfDay());
    }

    @GetMapping("/{departureStation}/{arrivalStation}/{flightDate}/groupby/searchdate")
    public Map<LocalDate, Flight> getFlightsForRouteAndDateGroupBySearchDate(@PathVariable String departureStation, @PathVariable String arrivalStation, @PathVariable String flightDate) {
        List<Flight> flights = getFlightsForRouteAndDate(departureStation, arrivalStation, flightDate);

        Map<LocalDate, Flight> flightsGroupBySearchDate = flights.stream().collect(toMap(
                flght -> flght.getSearchDateTime().toLocalDate(),
                flight -> flight,
                (flight1, flight2) -> {
                    double roundernAvgPrice = Double.valueOf((flight1.getPriceInHuf() + flight2.getPriceInHuf()) / 2).intValue();
                    return new Flight(flight1.getId(), flight1.getSearchDateTime(), flight1.getFlightDateTime(), flight1.getDepartureStation(),
                            flight1.getArrivalStation(), roundernAvgPrice);
                })
        );

        Map<LocalDate, Flight> flightsGroupBySearchDateInDescendingOrder = new TreeMap<>(reverseOrder());
        flightsGroupBySearchDateInDescendingOrder.putAll(flightsGroupBySearchDate);
        return flightsGroupBySearchDateInDescendingOrder;
    }
}
