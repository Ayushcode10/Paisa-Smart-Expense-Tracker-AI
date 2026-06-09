package com.paisa.backend.service;


import com.paisa.backend.config.AuthUtils;
import com.paisa.backend.entity.Category;
import com.paisa.backend.repository.mysql.CategoryRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService{

    //injecting dependency
    private final CategoryRepository categoryRepository;


    @PostConstruct
    public void seedDefaultCategories(){
        if(categoryRepository.existsByIsDefaultTrue()){
            return; //already seeded, skip
        }

        List<Category> defaults = List.of(
                Category.builder().name("Food").icon("🍔").color("#FF6B35").isDefault(true).build(),
                Category.builder().name("Travel").icon("✈️").color("#2196F3").isDefault(true).build(),
                Category.builder().name("Shopping").icon("🛍️").color("#9C27B0").isDefault(true).build(),
                Category.builder().name("Bills").icon("📄").color("#607D8B").isDefault(true).build(),
                Category.builder().name("Entertainment").icon("🎬").color("#E91E63").isDefault(true).build(),
                Category.builder().name("Health").icon("💊").color("#4CAF50").isDefault(true).build(),
                Category.builder().name("EMI").icon("🏦").color("#F44336").isDefault(true).build(),
                Category.builder().name("Salary").icon("💰").color("#8BC34A").isDefault(true).build(),
                Category.builder().name("Fuel").icon("⛽").color("#FF9800").isDefault(true).build(),
                Category.builder().name("Groceries").icon("🛒").color("#009688").isDefault(true).build(),
                Category.builder().name("Education").icon("📚").color("#3F51B5").isDefault(true).build(),
                Category.builder().name("Investment").icon("📈").color("#00BCD4").isDefault(true).build(),
                Category.builder().name("Uncategorized").icon("❓").color("#9E9E9E").isDefault(true).build()
        );

        categoryRepository.saveAll(defaults);
        log.info("seeded {} default categories", defaults.size());
    }

    public List<Category> getAllForUser(){
        Long userId = AuthUtils.getCurrentUserId();
        return categoryRepository.findAllForUser(userId);
    }

    public Category createCustomCategory(String name, String icon, String color){
        Long userId = AuthUtils.getCurrentUserId();

        //check for duplicate name for this user
        categoryRepository.findByNameAndUserId(name, userId).ifPresent(c -> {
            throw new RuntimeException("Category '" +  name + "' already exists");
        });

        Category category = Category.builder()
                .name(name)
                .icon(icon != null ? icon : "📌")
                .color(color != null ? color : "#9E9E9E")
                .isDefault(false)
                .userId(userId)
                .build();

        return categoryRepository.save(category);
    }

    public void deleteCategory(String name){
        Long userId = AuthUtils.getCurrentUserId();

        Category category = categoryRepository
                .findByNameAndUserId(name, userId)
                .orElseThrow(()-> new RuntimeException("Category not found"));

        if(category.isDefault()){
            throw new RuntimeException("System default categories cannot be deleted");
        }

        categoryRepository.delete(category);
    }
}