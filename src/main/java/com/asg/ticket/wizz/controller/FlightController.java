package com.asg.ticket.wizz.controller;

import com.asg.ticket.wizz.dto.Flight;
import com.asg.ticket.wizz.repository.FlightRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;

import static java.time.LocalDate.parse;
import static java.time.format.DateTimeFormatter.ISO_DATE;
import static java.util.Collections.reverseOrder;
import static java.util.stream.Collectors.groupingBy;
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

        Map<LocalDate, Flight> flightsGroupBySearchDate = flights.stream()
                .collect(toMapBySearchDate(flight -> flight.getSearchDateTime().toLocalDate()));

        Map<LocalDate, Flight> flightsGroupBySearchDateInDescendingOrder = new TreeMap<>(reverseOrder());
        flightsGroupBySearchDateInDescendingOrder.putAll(flightsGroupBySearchDate);
        return flightsGroupBySearchDateInDescendingOrder;
    }

    @GetMapping("/{departureStation}/{arrivalStation}/groupby/searchdate/flightdate")
    public Map<LocalDate, Map<LocalDate, Flight>> getFlightsForRouteGroupBySearchDateAndFlightDate(@PathVariable String departureStation, @PathVariable String arrivalStation) {
        List<Flight> flights = getFlightsForRoute(departureStation, arrivalStation);

        Map<LocalDate, Map<LocalDate, Flight>> flightsGroupBySearchDateAndFlightDate = flights.stream()
                .collect(groupingBy(
                        flight -> flight.getSearchDateTime().toLocalDate(),
                        toMapBySearchDate(flight -> flight.getFlightDateTime().toLocalDate())
        ));

        Map<LocalDate, Map<LocalDate, Flight>> flightsGroupBySearchDateAndFlightDateInDescendingOrder = new TreeMap<>(reverseOrder());
        flightsGroupBySearchDateAndFlightDateInDescendingOrder.putAll(flightsGroupBySearchDateAndFlightDate);
        return flightsGroupBySearchDateAndFlightDateInDescendingOrder;
    }

    private Collector<Flight, ?, Map<LocalDate, Flight>> toMapBySearchDate(Function<Flight, LocalDate> flightToDate) {
        return toMap(
                flightToDate,
                Function.identity(),
                (flight1, flight2) -> {
                    double roundernAvgPrice = Double.valueOf((flight1.getPriceInHuf() + flight2.getPriceInHuf()) / 2).intValue();
                    return new Flight(flight1.getId(), flight1.getSearchDateTime(), flight1.getFlightDateTime(), flight1.getDepartureStation(),
                            flight1.getArrivalStation(), roundernAvgPrice);
                });
    }
}
