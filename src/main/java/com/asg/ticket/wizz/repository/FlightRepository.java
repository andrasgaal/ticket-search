package com.asg.ticket.wizz.repository;

import com.asg.ticket.wizz.dto.Flight;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface FlightRepository extends MongoRepository<Flight, String>{
}
