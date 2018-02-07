package com.asg.ticket.wizz.repository;

import com.mongodb.client.DistinctIterable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.HashSet;
import java.util.Set;

public class CustomFlightRepositoryImpl implements CustomFlightRepository{

    @Autowired
    MongoTemplate mongoTemplate;

    @Override
    public Set<String> getIatas() {
        DistinctIterable<String> distinct = mongoTemplate.getCollection("flight").distinct("departureStation", String.class);
        Set<String> iatas = new HashSet<>();
        distinct.into(iatas);
        return iatas;
    }
}
