package org.example.canvasbackend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class CreateCanvasRequest {
    @Min(value = 1, message = "Width must be >= 1")
    @Max(value = 1000, message = "Width must be <= 1000")
    private int width;

    @Min(value = 1, message = "Height must be >= 1")
    @Max(value = 1000, message = "Height must be <= 1000")
    private int height;
}