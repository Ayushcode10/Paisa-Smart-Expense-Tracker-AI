package com.paisa.backend.controller;

import com.paisa.backend.dto.request.ContributeGoalRequest;
import com.paisa.backend.dto.request.CreateGoalRequest;
import com.paisa.backend.dto.response.GoalResponse;
import com.paisa.backend.service.GoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;

    @PostMapping
    public ResponseEntity<GoalResponse> create(@Valid @RequestBody CreateGoalRequest request){
        return ResponseEntity.ok(goalService.createGoal(request));
    }

    @GetMapping
    public ResponseEntity<List<GoalResponse>> getAll(){
        return ResponseEntity.ok(goalService.getAllGoals());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GoalResponse> getOne(@PathVariable Long id){
        return ResponseEntity.ok(goalService.getGoalById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GoalResponse> update(
            @PathVariable Long id,
            @RequestBody CreateGoalRequest request){
        return ResponseEntity.ok(goalService.updateGoal(id,request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id){
        goalService.deleteGoal(id);
        return ResponseEntity.ok(Map.of("message","Goal Deleted"));
    }

    @PostMapping("/{id}/contribute")
    public ResponseEntity<GoalResponse> contribute(
            @PathVariable Long id,
            @Valid @RequestBody ContributeGoalRequest request
            ){
        return ResponseEntity.ok(goalService.contributeToGoal(id,
                request.getAmount(),
                request.getNote()));
    }
}
