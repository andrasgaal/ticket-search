package com.asg.ticket.wizz.repository;

import com.asg.ticket.wizz.dto.Flight;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface FlightRepository extends MongoRepository<Flight, String>, CustomFlightRepository{

    @Query(value="{ 'departureStation': ?0, 'arrivalStation': ?1, 'flightDateTime': {$gte: ?2, $lt: ?3}}")
    List<Flight> getFlightsForRouteAndDate(String departureIata, String arrivalIata, LocalDateTime flightDateStartOfDay, LocalDateTime flightDateEndOfDay);

    @Query(value="{ 'departureStation': ?0, 'arrivalStation': ?1}}")
    List<Flight> getFlightsForRoute(String departureIata, String arrivalIata);

    @Query(value="{ 'departureStation': ?0, 'arrivalStation': ?1, 'flightDateTime': {$gte: ?2, $lt: ?3}, 'searchDateTime': {$gte: ?4}}}")
    List<Flight> getFlightsForRouteAfterSearchDatesBeforeFlightDate(String departureIata, String arrivalIata, LocalDateTime flightDatesFrom, LocalDateTime flightDatesUntil, LocalDateTime searchDatesFrom);
}
