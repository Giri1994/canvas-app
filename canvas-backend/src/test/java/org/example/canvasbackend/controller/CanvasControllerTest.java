package org.example.canvasbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.canvasbackend.Exception.CanvasException;
import org.example.canvasbackend.Exception.GlobalExceptionHandler;
import org.example.canvasbackend.Service.CanvasService;
import org.example.canvasbackend.dto.CanvasResponse;
import org.example.canvasbackend.dto.CreateCanvasRequest;
import org.example.canvasbackend.dto.FillRequest;
import org.example.canvasbackend.dto.LineRequest;
import org.example.canvasbackend.dto.RectangleRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller-layer tests using Mockito + standalone MockMvc.
 *
 * Mockito wires:
 *   @Mock         → creates a mock for CanvasService (the controller's dependency)
 *   @InjectMocks  → creates CanvasController and injects the @Mock CanvasService into it
 *
 * Each test follows:
 *   Given  – stub the mock service and build the HTTP request
 *   When   – perform the HTTP call via MockMvc
 *   Then   – assert HTTP status, JSON body, and mock interactions
 */
@ExtendWith(MockitoExtension.class)
class CanvasControllerTest {

    // ── Dependency mock ──────────────────────────────────────────
    @Mock
    private CanvasService canvasService;   // mocked dependency injected into the controller

    // ── System under test ────────────────────────────────────────
    @InjectMocks
    private CanvasController canvasController;  // Mockito injects @Mock canvasService here

    private MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUpMockMvc() {
        // Build standalone MockMvc wired to the controller + exception handler
        mockMvc = MockMvcBuilders
                .standaloneSetup(canvasController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    // POST /api/canvas/create
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/canvas/create")
    class CreateCanvasEndpointTests {

        @Test
        @DisplayName("Should return 200 and canvas response when dimensions are valid")
        void createCanvas_validRequest_returns200() throws Exception {
            // Given — stub the mock service to return a canned response
            CanvasResponse mockResponse = new CanvasResponse("------\n|    |\n------", "Canvas created", true);
            when(canvasService.executeCreateCanvas(eq("C 4 1"))).thenReturn(mockResponse);

            CreateCanvasRequest request = new CreateCanvasRequest();
            request.setWidth(4);
            request.setHeight(1);

            // When / Then
            mockMvc.perform(post("/api/canvas/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Canvas created"))
                    .andExpect(jsonPath("$.canvas").value("------\n|    |\n------"));

            verify(canvasService).executeCreateCanvas("C 4 1");
        }

        @Test
        @DisplayName("Should return 400 when width is below minimum (0)")
        void createCanvas_widthZero_returns400() throws Exception {
            // Given – width=0 violates @Min(1) constraint
            String requestBody = "{\"width\":0,\"height\":5}";

            // When / Then
            mockMvc.perform(post("/api/canvas/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));

            verifyNoInteractions(canvasService);
        }

        @Test
        @DisplayName("Should return 400 when height exceeds maximum (1001)")
        void createCanvas_heightExceedsMax_returns400() throws Exception {
            // Given – height=1001 violates @Max(1000) constraint
            String requestBody = "{\"width\":5,\"height\":1001}";

            // When / Then
            mockMvc.perform(post("/api/canvas/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));

            verifyNoInteractions(canvasService);
        }

        @Test
        @DisplayName("Should return 400 when request body is missing")
        void createCanvas_missingBody_returns400() throws Exception {
            // Given – no request body sent

            // When / Then
            mockMvc.perform(post("/api/canvas/create")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Error: Malformed or missing request body"));

            verifyNoInteractions(canvasService);
        }

        @Test
        @DisplayName("Should return 500 when service throws CanvasException")
        void createCanvas_serviceThrowsCanvasException_returns500() throws Exception {
            // Given – service throws CanvasException (not mapped to 400 in GlobalExceptionHandler)
            when(canvasService.executeCreateCanvas(anyString()))
                    .thenThrow(new CanvasException("Width must be >= 1"));

            String requestBody = "{\"width\":5,\"height\":5}";

            // When / Then
            mockMvc.perform(post("/api/canvas/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    // ─────────────────────────────────────────────────────────────
    // POST /api/canvas/line
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/canvas/line")
    class DrawLineEndpointTests {

        @Test
        @DisplayName("Should return 200 and updated canvas when line coordinates are valid")
        void drawLine_validRequest_returns200() throws Exception {
            // Given
            CanvasResponse mockResponse = new CanvasResponse("---\n|x|\n---", "Line drawn", true);
            when(canvasService.executeDrawLine(eq("L 1 1 1 1"))).thenReturn(mockResponse);

            LineRequest request = new LineRequest();
            request.setX1(1); request.setY1(1);
            request.setX2(1); request.setY2(1);

            // When / Then
            mockMvc.perform(post("/api/canvas/line")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Line drawn"));

            verify(canvasService).executeDrawLine("L 1 1 1 1");
        }

        @Test
        @DisplayName("Should return 400 when x1 is below minimum (0)")
        void drawLine_x1Zero_returns400() throws Exception {
            // Given – x1=0 violates @Min(1)
            String requestBody = "{\"x1\":0,\"y1\":1,\"x2\":5,\"y2\":1}";

            // When / Then
            mockMvc.perform(post("/api/canvas/line")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));

            verifyNoInteractions(canvasService);
        }

        @Test
        @DisplayName("Should return 400 when request body is malformed JSON")
        void drawLine_malformedJson_returns400() throws Exception {
            // Given
            String invalidJson = "{not valid json}";

            // When / Then
            mockMvc.perform(post("/api/canvas/line")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidJson))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Error: Malformed or missing request body"));
        }
    }

    // ─────────────────────────────────────────────────────────────
    // POST /api/canvas/rectangle
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/canvas/rectangle")
    class DrawRectangleEndpointTests {

        @Test
        @DisplayName("Should return 200 and updated canvas when rectangle coordinates are valid")
        void drawRectangle_validRequest_returns200() throws Exception {
            // Given
            CanvasResponse mockResponse = new CanvasResponse("some-canvas", "Rectangle drawn", true);
            when(canvasService.executeRectangleLine(eq("R 1 1 4 3"))).thenReturn(mockResponse);

            RectangleRequest request = new RectangleRequest();
            request.setX1(1); request.setY1(1);
            request.setX2(4); request.setY2(3);

            // When / Then
            mockMvc.perform(post("/api/canvas/rectangle")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Rectangle drawn"));

            verify(canvasService).executeRectangleLine("R 1 1 4 3");
        }

        @Test
        @DisplayName("Should return 400 when y2 is zero (violates @Min(1))")
        void drawRectangle_y2Zero_returns400() throws Exception {
            // Given
            String requestBody = "{\"x1\":1,\"y1\":1,\"x2\":4,\"y2\":0}";

            // When / Then
            mockMvc.perform(post("/api/canvas/rectangle")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));

            verifyNoInteractions(canvasService);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // POST /api/canvas/fill
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/canvas/fill")
    class BucketFillEndpointTests {

        @Test
        @DisplayName("Should return 200 when fill coordinates and color are valid")
        void bucketFill_validRequest_returns200() throws Exception {
            // Given
            CanvasResponse mockResponse = new CanvasResponse("filled-canvas", "Fill applied", true);
            when(canvasService.executeBucketFill(eq("B 2 3 o"))).thenReturn(mockResponse);

            FillRequest request = new FillRequest();
            request.setX(2); request.setY(3);
            request.setColor("o");

            // When / Then
            mockMvc.perform(post("/api/canvas/fill")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Fill applied"));

            verify(canvasService).executeBucketFill("B 2 3 o");
        }

        @Test
        @DisplayName("Should return 400 when color is blank")
        void bucketFill_blankColor_returns400() throws Exception {
            // Given – empty string violates @NotBlank
            String requestBody = "{\"x\":1,\"y\":1,\"color\":\"\"}";

            // When / Then
            mockMvc.perform(post("/api/canvas/fill")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));

            verifyNoInteractions(canvasService);
        }

        @Test
        @DisplayName("Should return 400 when color has more than one character")
        void bucketFill_multiCharColor_returns400() throws Exception {
            // Given – 'oo' violates @Size(max=1)
            String requestBody = "{\"x\":1,\"y\":1,\"color\":\"oo\"}";

            // When / Then
            mockMvc.perform(post("/api/canvas/fill")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));

            verifyNoInteractions(canvasService);
        }

        @Test
        @DisplayName("Should return 400 when x coordinate is zero")
        void bucketFill_xZero_returns400() throws Exception {
            // Given
            String requestBody = "{\"x\":0,\"y\":1,\"color\":\"o\"}";

            // When / Then
            mockMvc.perform(post("/api/canvas/fill")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));

            verifyNoInteractions(canvasService);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // POST /api/canvas/quit
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/canvas/quit")
    class QuitEndpointTests {

        @Test
        @DisplayName("Should return 200 and reset message when quit is called")
        void quit_validCall_returns200() throws Exception {
            // Given
            CanvasResponse mockResponse = new CanvasResponse("", "Canvas reset", true);
            when(canvasService.executeQuit(eq("Q"))).thenReturn(mockResponse);

            // When / Then
            mockMvc.perform(post("/api/canvas/quit")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Canvas reset"))
                    .andExpect(jsonPath("$.canvas").value(""));

            verify(canvasService).executeQuit("Q");
        }

        @Test
        @DisplayName("Should always call service.executeQuit with 'Q' regardless of body")
        void quit_alwaysPassesQCommand() throws Exception {
            // Given
            when(canvasService.executeQuit("Q"))
                    .thenReturn(new CanvasResponse("", "Canvas reset", true));

            // When
            mockMvc.perform(post("/api/canvas/quit")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))  // extra body is ignored
                    .andExpect(status().isOk());

            // Then
            verify(canvasService, times(1)).executeQuit("Q");
            verifyNoMoreInteractions(canvasService);
        }
    }
}
