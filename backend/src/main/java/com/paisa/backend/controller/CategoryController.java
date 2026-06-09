package com.paisa.backend.controller;

import com.paisa.backend.dto.request.CreateCategoryRequest;
import com.paisa.backend.entity.Category;
import com.paisa.backend.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<Category>> getAll(){
        return ResponseEntity.ok(categoryService.getAllForUser());
    }

    @PostMapping
    public ResponseEntity<Category> create( @RequestBody CreateCategoryRequest request){
        return ResponseEntity.ok(
                categoryService.createCustomCategory(
                        request.getName(),
                        request.getIcon(),
                        request.getColor()
                )
        );

    }

    @DeleteMapping("/{name}")
    public ResponseEntity<?> delete(@PathVariable String name){
        categoryService.deleteCategory(name);
        return ResponseEntity.ok(Map.of("message", "Category deleted"));
    }
}
