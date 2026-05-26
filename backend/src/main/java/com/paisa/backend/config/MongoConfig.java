package com.paisa.backend.config;

import com.paisa.backend.document.Transaction;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.Index;
import org.springframework.data.mongodb.core.index.IndexOperations;

@Configuration
@RequiredArgsConstructor
public class MongoConfig {
    private final MongoTemplate mongoTemplate;


    @PostConstruct
    public void initIndexes(){
        //these make queries like "find by userId sorted by date" very fast
        //w/o indexes, MongoDB scans every doc - slow with larger no. of transactions

        IndexOperations indexOps =
                mongoTemplate.indexOps("transactions");

        indexOps.createIndex(new Index()
                .on("userId", Sort.Direction.ASC));

        indexOps.createIndex(new Index()
                .on("transactionDate", Sort.Direction.DESC));

        indexOps.createIndex(new Index()
                .on("userId", Sort.Direction.ASC)
                .on("transactionDate", Sort.Direction.DESC));

    }
}
