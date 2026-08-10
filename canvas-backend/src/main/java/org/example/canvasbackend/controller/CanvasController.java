package org.example.canvasbackend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import org.example.canvasbackend.Service.CanvasService;
import org.example.canvasbackend.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/canvas")
public class CanvasController {

    private CanvasService canvasService;

    @Autowired
    CanvasController(CanvasService canvasService) {
        this.canvasService = canvasService;
    }


    @PostMapping("/create")
    @Operation(summary = "Create a new canvas", description = "Creates a new canvas with the specified width and height.", method = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Canvas created", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CanvasResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<CanvasResponse> createCanvas(@Valid @RequestBody CreateCanvasRequest request) {
        String inputVal = String.format("C %d %d", request.getWidth(), request.getHeight());
        return ResponseEntity.ok(canvasService.executeCreateCanvas(inputVal));
    }


    @PostMapping("/line")
    @Operation(summary = "Draw line", description = "Draws a horizontal or vertical line from (x1,y1) to (x2,y2).", method = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Line drawn", content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "Draw Line",
                            value = "{\"x1\":1,\"y1\":2,\"x2\":6,\"y2\":2}"
                            ), schema = @Schema(implementation = CanvasResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or canvas state", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<CanvasResponse> drawLine(@Valid @RequestBody LineRequest request) {
        String inputValLine = String.format("L %d %d %d %d", request.getX1(), request.getY1(), request.getX2(), request.getY2());
        return ResponseEntity.ok(canvasService.executeDrawLine(inputValLine));
    }

    @PostMapping("/rectangle")
    @Operation(
            summary = "Draw rectangle",
            description = "Draws a rectangle using top-left (x1,y1) and bottom-right (x2,y2).",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Draw Rectangle",
                                    value = "{\"x1\":14,\"y1\":1,\"x2\":18,\"y2\":3}"
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Rectangle drawn", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CanvasResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or canvas state", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<CanvasResponse> drawRectangle(@Valid @RequestBody RectangleRequest request) {
        String inputValRectangle = String.format("R %d %d %d %d", request.getX1(), request.getY1(), request.getX2(), request.getY2());
        return ResponseEntity.ok(canvasService.executeRectangleLine(inputValRectangle));
    }

    @PostMapping("/fill")
    @Operation(
            summary = "Bucket fill",
            description = "Fills connected area from (x,y) with a single character color.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Bucket Fill",
                                    value = "{\"x\":10,\"y\":3,\"color\":\"o\"}"
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Fill applied", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CanvasResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input or canvas state", content = @Content(mediaType = "application/json"))
    })
    public ResponseEntity<CanvasResponse> bucketFill(@Valid @RequestBody FillRequest request) {
        String inputValBucketFill = String.format("B %d %d %s", request.getX(), request.getY(), request.getColor());
        return ResponseEntity.ok(canvasService.executeBucketFill(inputValBucketFill));
    }

    @PostMapping("/quit")
    @Operation(
            summary = "Quit",
            description = "Resets canvas state for current runtime/session.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = false,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Quit",
                                    value = "{}"
                            )
                    )
            )
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Canvas reset", content = @Content(mediaType = "application/json", schema = @Schema(implementation = CanvasResponse.class)))
    })
    public ResponseEntity<CanvasResponse> quit() {
        return ResponseEntity.ok(canvasService.executeQuit("Q"));
    }
}