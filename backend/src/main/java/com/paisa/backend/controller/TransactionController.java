package com.paisa.backend.controller;

import com.paisa.backend.dto.request.CreateTransactionRequest;
import com.paisa.backend.dto.request.UpdateTransactionRequest;
import com.paisa.backend.dto.response.TransactionResponse;
import com.paisa.backend.dto.response.TransactionSummaryResponse;
import com.paisa.backend.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Date;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    //POST /api/transactions

    @PostMapping
    public ResponseEntity<TransactionResponse> create(
            @Valid @RequestBody CreateTransactionRequest request){
        return ResponseEntity.ok(transactionService.createTransaction(request));
    }

    //GET /api/transactions?page=0&size=20category=food@from=..to=...
    @GetMapping
    public ResponseEntity<Page<TransactionResponse>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String category,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to){

        return ResponseEntity.ok(transactionService.getTransaction(page,size,category,from,to));
    }

    //GET /api/transactions/{id}

    @GetMapping("/{id}")
    public ResponseEntity<TransactionResponse> getOne(@PathVariable String id){
        return ResponseEntity.ok(transactionService.getTransactionById(id));
    }

    //PUT /api/transactions/{id}

    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> update(
            @PathVariable String id,
            @RequestBody UpdateTransactionRequest request){
        return ResponseEntity.ok(transactionService.updateTransaction(id,request));
    }

    //DELETE /api/transactions/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id){
        transactionService.deleteTransaction(id);
        return ResponseEntity.ok().body("Transaction Deleted");
    }

    //GET /api/transactions/summary?from=..&to=..
    @GetMapping("/summary")
    public ResponseEntity<TransactionSummaryResponse> summary(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to){
        return ResponseEntity.ok(transactionService.getSummary(from,to));
    }

}
