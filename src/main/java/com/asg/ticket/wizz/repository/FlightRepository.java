package com.asg.ticket.wizz.repository;

import com.asg.ticket.wizz.dto.Flight;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface FlightRepository extends MongoRepository<Flight, String>, CustomFlightRepository{

    @Query(value="{ 'departureStation' : ?0, 'arrivalStation' : ?1, 'flightDateTime' : {$gte: ?2, $lt: ?3}}")
    List<Flight> getFlights(String departureIata, String arrivalIata, LocalDateTime flightDateStartOfDay, LocalDateTime flightDateEndOfDay);
}
