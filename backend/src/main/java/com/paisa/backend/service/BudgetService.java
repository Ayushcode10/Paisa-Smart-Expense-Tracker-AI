package com.paisa.backend.service;


import com.paisa.backend.config.AuthUtils;
import com.paisa.backend.document.Transaction;
import com.paisa.backend.dto.request.CreateBudgetRequest;
import com.paisa.backend.dto.request.UpdateBudgetRequest;
import com.paisa.backend.dto.response.BudgetResponse;
import com.paisa.backend.entity.Budget;
import com.paisa.backend.repository.BudgetRepository;
import com.paisa.backend.repository.mongo.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final TransactionRepository transactionRepository;

    public BudgetResponse createBudget(CreateBudgetRequest request){
        Long userId = AuthUtils.getCurrentUserId();

        LocalDate[] period = calculatePeriodDates(request.getPeriod());

        Budget budget = Budget.builder()
                .userId(userId)
                .category(request.getCategory())
                .limitAmount(request.getLimitAmount())
                .period(request.getPeriod())
                .rollover(request.isRollover())
                .alertAt(request.getAlertAt() != null ? request.getAlertAt() : 80)
                .periodStart(period[0])
                .periodEnd(period[1])
                .build();

        Budget saved =  budgetRepository.save(budget);
        return toResponse(saved,userId);
    }

    public List<BudgetResponse> getAllBudgets(){
        Long userId  = AuthUtils.getCurrentUserId();
        return budgetRepository.findByUserId(userId)
                .stream()
                .map(b -> toResponse(b,userId))
                .collect(Collectors.toList());
    }

    public BudgetResponse getBudgetById(Long id){
        Long userId = AuthUtils.getCurrentUserId();
        Budget budget = budgetRepository.findByIdAndUserId(id, userId)
                .orElseThrow(()-> new RuntimeException("Budget Not Found"));

        return toResponse(budget,userId);
    }

    public BudgetResponse updateBudget(Long id, UpdateBudgetRequest request){
        Long userId = AuthUtils.getCurrentUserId();
        Budget budget = budgetRepository.findByIdAndUserId(id,userId)
                .orElseThrow(()->new RuntimeException("Budget Not Found"));

        if (request.getLimitAmount() != null) budget.setLimitAmount(request.getLimitAmount());
        if (request.getAlertAt() != null) budget.setAlertAt(request.getAlertAt());
        if (request.getRollover() != null) budget.setRollover(request.getRollover());

        return toResponse(budgetRepository.save(budget), userId);
    }

    public void deleteBudget(Long id){
        Long userId = AuthUtils.getCurrentUserId();
        Budget budget = budgetRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Budget Not Found"));

        budgetRepository.delete(budget);
    }

    // ─────────────────────────────────────────────
    // Called by the alert scheduler to check if
    // any budget has crossed its alert threshold
    // ─────────────────────────────────────────────

    public boolean isBudgetBreached(Long userId, String category, String period){
        return budgetRepository
                .findByUserIdCategoryAndPeriod(userId,category,period)
                .map(budget -> {
                    BigDecimal spent = calculateSpent(userId,category,budget);
                    double percent = spent.divide(budget.getLimitAmount(), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .doubleValue();
                    return percent >= budget.getAlertAt();
                }).orElse(false);
    }

    // ─────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────

    // Converts Budget entity → BudgetResponse DTO
    // Also calculates live spend from MongoDB

    private BudgetResponse toResponse(Budget budget, Long userId){
        BigDecimal spent = calculateSpent(userId,budget.getCategory(), budget);
        BigDecimal limit = budget.getLimitAmount();
        BigDecimal remainingAmount = limit.subtract(spent).max(BigDecimal.ZERO);

        double percentUsed = spent
                .divide(limit, 4,RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();

        //Determine status for UI color-coding
        String status;
        if (percentUsed >= 100) status = "EXCEEDED"; //red
        else if(percentUsed >= budget.getAlertAt()) status = "Warning"; //orange/yellow
        else status = "On_track";  //green

        return BudgetResponse.builder()
                .id(budget.getId())
                .category(budget.getCategory())
                .limitAmount(limit)
                .spentAmount(spent)
                .remainingAmount(remainingAmount)
                .percentUsed(Math.min(percentUsed,100.0))
                .status(status)
                .period(budget.getPeriod())
                .rollover(budget.isRollover())
                .alertAt(budget.getAlertAt())
                .periodStart(budget.getPeriodStart())
                .periodEnd(budget.getPeriodEnd())
                .build();
    }

    //Queries Mongodb to get real spend for this category in this period
    private BigDecimal calculateSpent(Long userId, String category, Budget budget){
        LocalDateTime start = budget.getPeriodStart().atStartOfDay();
        LocalDateTime end = budget.getPeriodEnd().atTime(23,59,59);

        List<Transaction> transactions = transactionRepository
                .findByUserIdAndTransactionDateBetween(userId, start, end);

        return transactions.stream()
                .filter(t -> "DEBIT".equals(t.getType()))
                .filter(t -> category.equalsIgnoreCase(t.getCategory()))
                .map(Transaction::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private LocalDate[] calculatePeriodDates(String period){
        LocalDate today = LocalDate.now();

        if("WEEKLY".equals(period)){
            //monday to sunday of current week
            LocalDate monday = today.with(DayOfWeek.MONDAY);
            LocalDate sunday = today.with(DayOfWeek.SUNDAY);
            return new LocalDate[]{monday,sunday};
        }

        //Default: MONTHLY - 1st to last day of current month
        LocalDate start = today.withDayOfMonth(1);
        LocalDate end = today.withDayOfMonth(today.lengthOfMonth());
        return new LocalDate[]{start,end};
    }
}
