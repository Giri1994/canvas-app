package org.example.canvasbackend.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class RectangleRequest {
    @Min(value = 1, message = "x1 must be >= 1")
    private int x1;

    @Min(value = 1, message = "y1 must be >= 1")
    private int y1;

    @Min(value = 1, message = "x2 must be >= 1")
    private int x2;

    @Min(value = 1, message = "y2 must be >= 1")
    private int y2;
}