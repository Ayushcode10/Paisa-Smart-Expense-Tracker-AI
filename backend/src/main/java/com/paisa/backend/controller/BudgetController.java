package com.paisa.backend.controller;

import com.paisa.backend.dto.request.CreateBudgetRequest;
import com.paisa.backend.dto.request.UpdateBudgetRequest;
import com.paisa.backend.dto.response.BudgetResponse;
import com.paisa.backend.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @PostMapping
    public ResponseEntity<BudgetResponse> create(@Valid @RequestBody CreateBudgetRequest request){
        return ResponseEntity.ok(budgetService.createBudget(request));
    }

    @GetMapping
    public ResponseEntity<List<BudgetResponse>> getAll(){
        return ResponseEntity.ok(budgetService.getAllBudgets());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BudgetResponse> getOne(@PathVariable Long id){
        return ResponseEntity.ok(budgetService.getBudgetById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetResponse> update(
            @PathVariable Long id,
            @RequestBody UpdateBudgetRequest request){
        return ResponseEntity.ok(budgetService.updateBudget(id,request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id){
        budgetService.deleteBudget(id);
        return ResponseEntity.ok(Map.of("message", "Budget Deleted"));
    }
}
