package com.paisa.backend.service;

import com.paisa.backend.config.AuthUtils;
import com.paisa.backend.document.Transaction;
import com.paisa.backend.dto.request.CreateTransactionRequest;
import com.paisa.backend.dto.request.UpdateTransactionRequest;
import com.paisa.backend.dto.response.TransactionResponse;
import com.paisa.backend.dto.response.TransactionSummaryResponse;
import com.paisa.backend.repository.mongo.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.Store;
import org.hibernate.id.BulkInsertionCapableIdentifierGenerator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {
    private final TransactionRepository transactionRepository;

    // ─────────────────────────────────────────────
    //                  CREATE
    // ─────────────────────────────────────────────

    public TransactionResponse createTransaction(CreateTransactionRequest request){
        //gets userId from jwt - no need to pass it in the req body
        Long userId = AuthUtils.getCurrentUserId();

        Transaction transaction = Transaction.builder()
                .userId(userId)
                .amount(request.getAmount())
                .merchant(request.getMerchant())
                .merchantNormalized(normalizeMerchant(request.getMerchant()))
                .category(request.getCategory() != null ? request.getCategory() : "Uncategorized")
                .type(request.getType())
                .source(request.getSource())
                .paymentMethod(request.getPaymentMethod())
                .note(request.getNote())
                .tags(request.getTags())
                .currency("INR")
                .smsImported(false)         //cuz manually created
                .transactionDate(
                        request.getTransactionDate() != null
                                ? request.getTransactionDate()
                                : LocalDateTime.now()
                )
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Transaction saved = transactionRepository.save(transaction);
        log.info("Transaction created: {} for user: {}", saved.getId(), userId);

        return toResponse(saved);
    }

    // ─────────────────────────────────────────────
    // GET ALL (with filters + pagination)
    // ─────────────────────────────────────────────

    public Page<TransactionResponse> getTransaction(
            int page,
            int size,
            String category,
            LocalDateTime from,
            LocalDateTime to
    ){
        Long userId = AuthUtils.getCurrentUserId();

        //pageable tells mongodb: to give page X with Y items sorted in desc order by date
        Pageable pageable = PageRequest.of(page, size, Sort.by("transactionDate").descending());

        Page<Transaction> results;

        //apply filters depending on what frontend sent
        if(category != null && from != null && to != null){
            // TODO: add this query if needed - for now filter in memory
            results = transactionRepository.findByUserIdAndCategory(userId,category,pageable);
        }else if(from != null && to != null){
            results = transactionRepository.findByUserIdAndTransactionDateBetween(userId, from, to, pageable);
        }else if(category != null){
            results = transactionRepository.findByUserIdAndCategory(userId,category,pageable);
        }else{
            results = transactionRepository.findByUserId(userId, pageable);
        }
        return results.map(this::toResponse);
    }

    // ─────────────────────────────────────────────
    //  GET ONE
    // ─────────────────────────────────────────────

    public TransactionResponse getTransactionById(String id){
        Long userId = AuthUtils.getCurrentUserId();

        //findByIdAndUserId -> ensures user can only see their own transactions

        Transaction transaction = transactionRepository.
                findByIdAndUserId(id,userId)
                .orElseThrow(()-> new RuntimeException("Transaction Not Found"));

        return toResponse(transaction);
    }

    // ─────────────────────────────────────────────
    //  UPDATE
    // ─────────────────────────────────────────────

    public TransactionResponse updateTransaction(String id, UpdateTransactionRequest request){
        Long userId = AuthUtils.getCurrentUserId();

        Transaction transaction = transactionRepository
                .findByIdAndUserId(id,userId)
                .orElseThrow(()-> new RuntimeException("Transaction Not Found"));

        if(request.getAmount() != null) transaction.setAmount(request.getAmount());
        if(request.getMerchant() != null) {
            transaction.setMerchant(request.getMerchant());
            transaction.setMerchantNormalized(normalizeMerchant(transaction.getMerchant()));
        }
        if(request.getCategory() != null) transaction.setCategory(request.getCategory());
        if(request.getType() != null) transaction.setType(request.getType());
        if(request.getSource() != null) transaction.setSource(request.getSource());
        if(request.getNote() != null) transaction.setNote(request.getNote());
        if(request.getTags() != null) transaction.setTags(request.getTags());

        transaction.setUpdatedAt(LocalDateTime.now());

        Transaction updated = transactionRepository.save(transaction);
        return toResponse(updated);
    }

    // ─────────────────────────────────────────────
    //  DELETE
    // ─────────────────────────────────────────────

    public void deleteTransaction(String id){
        Long userId = AuthUtils.getCurrentUserId();

        Transaction transaction = transactionRepository
                .findByIdAndUserId(id,userId)
                .orElseThrow(()-> new RuntimeException("Transaction not found"));

        transactionRepository.delete(transaction);
        log.info("Transaction {} deleted by user {}", id, userId);
    }


    // ─────────────────────────────────────────────
    //  SUMMARY FOR DASHBOARD
    // ─────────────────────────────────────────────

    public TransactionSummaryResponse getSummary(LocalDateTime from, LocalDateTime to){
        Long userId = AuthUtils.getCurrentUserId();

        //get all transaction in date range ( no pagination cuz we just need to calculate totals)
        List<Transaction> transactions = transactionRepository.findByUserIdAndTransactionDateBetween(userId,from,to);

        // separate Debits and Credits
        BigDecimal totalDebit = transactions.stream()
                .filter(t -> "DEBIT".equals(t.getType()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCredit = transactions.stream()
                .filter(t-> "CREDIT".equals(t.getType()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO,BigDecimal::add);

        // groups debits by category -> {"food" : 3200, "Travel" : 1500}
        Map<String, BigDecimal> spendByCategory = transactions.stream()
                .filter(t-> "DEBIT".equals(t.getType()))
                .collect(Collectors.groupingBy(
                        t-> t.getCategory() != null ? t.getCategory() : "Uncategorized",
                        Collectors.reducing(BigDecimal.ZERO,Transaction::getAmount, BigDecimal::add)
                ));

        // group debits by source -> {"upi" : 200 , "card": 1000}

        Map<String, BigDecimal> spendBySource = transactions.stream()
                .filter(t -> "DEBIT".equals(t.getType()))
                .collect(Collectors.groupingBy(
                        Transaction::getSource,
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)
                ));

        return TransactionSummaryResponse.builder()
                .totalDebit(totalDebit)
                .totalCredit(totalCredit)
                .netBalance(totalCredit.subtract(totalDebit))
                .transactionCount(transactions.size())
                .spendByCategory(spendByCategory)
                .spendBySource(spendBySource)
                .build();
    }

    // ─────────────────────────────────────────────
    //               PRIVATE HELPERS
    // ─────────────────────────────────────────────



    //Converts DB document to Response DTO
    //having this in one place means if you ever change the response shape,
    //you only change it here - not in every method
    private TransactionResponse toResponse(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .amount(t.getAmount())
                .merchant(t.getMerchant())
                .category(t.getCategory())
                .type(t.getType())
                .source(t.getSource())
                .paymentMethod(t.getPaymentMethod())
                .note(t.getNote())
                .tags(t.getTags())
                .smsImported(t.isSmsImported())
                .transactionDate(t.getTransactionDate())
                .createdAt(t.getCreatedAt())
                .build();
    }

    // "ZOMATO*ORDER #12345" → "zomato"
    // Helps with grouping and display
    private String normalizeMerchant(String merchant) {
        if(merchant == null) return "";
        return merchant
                .toLowerCase()
                .replaceAll("[^a-z0-9 ]","") //remove special chars
                .trim()
                .split(" ")[0];         //take first word only
    }

}
