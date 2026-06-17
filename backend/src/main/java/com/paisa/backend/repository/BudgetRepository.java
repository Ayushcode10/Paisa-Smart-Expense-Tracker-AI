package com.paisa.backend.repository;


import com.paisa.backend.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {

    List<Budget> findByUserId(Long userId);

    Optional<Budget> findByIdAndUserId(Long id, Long userId);


    //alert scheduler: find all budgets for a specific category and user
    Optional<Budget> findByUserIdCategoryAndPeriod(Long userId, String category, String period);
}
