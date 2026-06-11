package com.paisa.backend.repository;


import com.paisa.backend.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByUserId(Long userId);

    Optional<Budget> findByIdAndUserId(Long id, Long userId);


    //alert scheduler: find all budgets for a specific category and user
    Optional<Budget> findByUserIdCategoryAndPeriod(Long userId, String category, String period);
}
