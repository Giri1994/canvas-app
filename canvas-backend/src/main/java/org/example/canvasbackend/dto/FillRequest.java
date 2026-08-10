package org.example.canvasbackend.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class FillRequest {
    @Min(value = 1, message = "x must be >= 1")
    private int x;

    @Min(value = 1, message = "y must be >= 1")
    private int y;

    @NotBlank(message = "color is required")
    @Size(min = 1, max = 1, message = "color must be a single character")
    private String color;
}