package com.paisa.backend.service;

import com.paisa.backend.config.AuthUtils;
import com.paisa.backend.dto.request.CreateGoalRequest;
import com.paisa.backend.dto.response.GoalResponse;
import com.paisa.backend.entity.Goal;
import com.paisa.backend.repository.mysql.GoalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepository;

    public GoalResponse createGoal(CreateGoalRequest request){
        Long userId = AuthUtils.getCurrentUserId();

        Goal goal = Goal.builder()
                .userId(userId)
                .name(request.getName())
                .targetAmount(request.getTargetAmount())
                .currentAmount(request.getCurrentAmount() != null
                        ? request.getCurrentAmount()
                        : BigDecimal.ZERO)
                .targetDate(request.getTargetDate())
                .monthlyContribution(request.getMonthlyContribution())
                .icon(request.getIcon() != null ? request.getIcon() : "🎯")
                .status("ACTIVE")
                .build();

        return toResponse(goalRepository.save(goal));
    }

    public List<GoalResponse> getAllGoals(){
        Long userId = AuthUtils.getCurrentUserId();
        return goalRepository.findByUserId(userId)
                .stream()
                .map(this :: toResponse)
                .collect(Collectors.toList());
    }

    public GoalResponse getGoalById(Long id) {
        Long userId = AuthUtils.getCurrentUserId();
        Goal goal = goalRepository.findByIdAndUserId(id,userId)
                .orElseThrow(()-> new RuntimeException("Goal Not Found"));

        return toResponse(goal);
    }

    public GoalResponse contributeToGoal(Long id, BigDecimal amount, String note){
        Long userId = AuthUtils.getCurrentUserId();

        Goal goal = goalRepository.findByIdAndUserId(id,userId)
                .orElseThrow(()->new RuntimeException("Goal Not Found"));

        if("COMPLETED".equals(goal.getStatus())){
            throw new RuntimeException("Goal is already completed");
        }

        //add contribution
        goal.setCurrentAmount(goal.getCurrentAmount().add(amount));


        //auto mark as completed if target reached
        if(goal.getCurrentAmount().compareTo(goal.getTargetAmount())>=0){
            goal.setStatus("COMPLETED");
            log.info("Goal '{}' completed for user {}",goal.getName(),userId);
        }
        return toResponse(goalRepository.save(goal));
    }


    public GoalResponse updateGoal(Long id, CreateGoalRequest request) {
        Long userId = AuthUtils.getCurrentUserId();

        Goal goal = goalRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Goal not found"));

        if (request.getName() != null)               goal.setName(request.getName());
        if (request.getTargetAmount() != null)        goal.setTargetAmount(request.getTargetAmount());
        if (request.getTargetDate() != null)          goal.setTargetDate(request.getTargetDate());
        if (request.getMonthlyContribution() != null) goal.setMonthlyContribution(request.getMonthlyContribution());
        if (request.getIcon() != null)                goal.setIcon(request.getIcon());

        return toResponse(goalRepository.save(goal));
    }

    public void deleteGoal(Long id) {
        Long userId = AuthUtils.getCurrentUserId();
        Goal goal = goalRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new RuntimeException("Goal not found"));
        goalRepository.delete(goal);
    }


    private GoalResponse toResponse(Goal goal){
        BigDecimal target = goal.getTargetAmount();
        BigDecimal current = goal.getCurrentAmount();
        BigDecimal remaining = target.subtract(current).max(BigDecimal.ZERO);

        //How Many months left until target date
        LocalDate today = LocalDate.now();
        int monthsRemaining = Math.max(0,
                Period.between(today,goal.getTargetDate()).getMonths()
                +Period.between(today,goal.getTargetDate()).getYears() * 12
        );

        //how much per month is needed to hit that goal
        BigDecimal requiredMonthly = monthsRemaining > 0
                ? remaining.divide(BigDecimal.valueOf(monthsRemaining),2, RoundingMode.CEILING)
                :remaining; //due this month, need full amount;

        //percent complete  (current/target)*100;
        double percent = current
                .divide(target,4,RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .doubleValue();

        //are they on track ?
        boolean onTrack = goal.getMonthlyContribution() == null
                || goal.getMonthlyContribution().compareTo(requiredMonthly) >= 0;

        return GoalResponse.builder()
                .id(goal.getId())
                .name(goal.getName())
                .targetAmount(target)
                .currentAmount(current)
                .remainingAmount(remaining)
                .percentComplete(percent)
                .targetDate(goal.getTargetDate())
                .monthsRemaining(monthsRemaining)
                .requiredMonthlySaving(requiredMonthly)
                .onTrack(onTrack)
                .icon(goal.getIcon())
                .status(goal.getStatus())
                .createdAt(goal.getCreatedAt())
                .build();
    }
}
