package com.paisa.backend.dto.request;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCategoryRequest {

    @NotBlank(message = "Category name is required")
    private String name;

    private String icon;
    private String color;
}
