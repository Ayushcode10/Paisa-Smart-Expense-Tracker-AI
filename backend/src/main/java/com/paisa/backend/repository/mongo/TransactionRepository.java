package com.paisa.backend.repository.mongo;

import ch.qos.logback.core.util.Loader;
import com.paisa.backend.document.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.web.PageableArgumentResolver;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


//MongoRepository<Transaction, String>
// --> first arg = the doc type;
// --> second arg = the Id type (MongoDb uses it as a string)
public interface TransactionRepository extends MongoRepository<Transaction,String> {

    //spring reads the method name and writes the query automatically
    //"find all where userId = ? and TransactionDate btw ? and ?"

    Page<Transaction> findByUserIdAndTransactionDateBetween(
            Long userId,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );

    //"find all where userId = ? and Category = ?"
    Page<Transaction> findByUserIdAndCategory(
            Long userId,
            String category,
            Pageable pageable
    );

    //"find all where userid = ?"
    Page<Transaction> findByUserId(Long userId, Pageable pageable);

    //used for summary stats
    List<Transaction> findByUserIdAndTransactionDateBetween(
            Long userId,
            LocalDateTime start,
            LocalDateTime end
    );

    // check a specific transaction belongs to user
    Optional<Transaction> findByIdAndUserId(String id, Long userId);

    //count transactions per user
    long countByUserId(Long userId);
}
