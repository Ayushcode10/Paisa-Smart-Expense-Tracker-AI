package com.paisa.backend.repository.mongo;

import com.paisa.backend.document.Transaction;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface TransactionRepository extends MongoRepository<Transaction,String> {
    List<Transaction> findByUserId(Long userId);
}
