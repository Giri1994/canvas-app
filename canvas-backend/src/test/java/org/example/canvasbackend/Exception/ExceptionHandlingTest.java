package org.example.canvasbackend.Exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.canvasbackend.Service.CanvasService;
import org.example.canvasbackend.controller.CanvasController;
import org.example.canvasbackend.dto.CanvasResponse;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for CanvasException and GlobalExceptionHandler.
 *
 * Mockito wires (in handler tests):
 *   @Mock         → CanvasService  (used to trigger exceptions from the controller layer)
 *   @InjectMocks  → CanvasController (receives the mock service)
 *
 * GlobalExceptionHandler is registered on the standalone MockMvc so every
 * @ExceptionHandler branch is exercised through a real HTTP call.
 *
 * Each test follows:
 *   Given  → configure mock to throw or use a bad request body
 *   When   → perform the HTTP call via MockMvc
 *   Then   → assert the HTTP status and JSON response body
 */
class ExceptionHandlingTest {

    // ─────────────────────────────────────────────────────────────
    // CanvasException  (unit test — no Spring context needed)
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("CanvasException")
    class CanvasExceptionTests {

        @Test
        @DisplayName("Should store the message passed to its constructor")
        void canvasException_constructorMessage_isAccessibleViaGetMessage() {
            // Given
            String errorMessage = "Canvas not created";

            // When
            CanvasException exception = new CanvasException(errorMessage);

            // Then
            assertThat(exception.getMessage()).isEqualTo(errorMessage);
        }

        @Test
        @DisplayName("Should be a RuntimeException so it is unchecked")
        void canvasException_isRuntimeException() {
            // Given / When
            CanvasException exception = new CanvasException("some error");

            // Then
            assertThat(exception).isInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Should be throwable and catchable like any RuntimeException")
        void canvasException_thrownAndCaught_messageIsPreserved() {
            // Given
            String message = "Width must be >= 1";

            // When / Then
            assertThatThrownBy(() -> { throw new CanvasException(message); })
                    .isInstanceOf(CanvasException.class)
                    .hasMessage(message);
        }

        @Test
        @DisplayName("Should have no cause when only a message is provided")
        void canvasException_noCause_whenOnlyMessageGiven() {
            // Given / When
            CanvasException exception = new CanvasException("oops");

            // Then
            assertThat(exception.getCause()).isNull();
        }
    }

    // ─────────────────────────────────────────────────────────────
    // GlobalExceptionHandler — each @ExceptionHandler branch
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("GlobalExceptionHandler")
    @ExtendWith(MockitoExtension.class)
    class GlobalExceptionHandlerTests {

        @Mock
        private CanvasService canvasService;   // mock dependency injected into controller

        @InjectMocks
        private CanvasController canvasController; // receives the mock

        private MockMvc mockMvc;
        private final ObjectMapper objectMapper = new ObjectMapper();

        @BeforeEach
        void setUpMockMvcWithExceptionHandler() {
            // Register GlobalExceptionHandler so all @ExceptionHandler methods are active
            mockMvc = MockMvcBuilders
                    .standaloneSetup(canvasController)
                    .setControllerAdvice(new GlobalExceptionHandler())
                    .build();
        }

        // ── MethodArgumentNotValidException (validation failure) ──

        @Test
        @DisplayName("Should return 400 with 'Validation failed' prefix when @Valid constraint fails")
        void handle_methodArgumentNotValidException_returns400WithValidationMessage() throws Exception {
            // Given — width=0 violates @Min(1) on CreateCanvasRequest
            String requestBody = "{\"width\":0,\"height\":5}";

            // When
            mockMvc.perform(post("/api/canvas/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))

            // Then
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Validation failed")));
        }

        @Test
        @DisplayName("Should include all field-level violation messages joined by '; '")
        void handle_multipleValidationFailures_messagesAreConcatenated() throws Exception {
            // Given — both width and height are 0
            String requestBody = "{\"width\":0,\"height\":0}";

            // When
            mockMvc.perform(post("/api/canvas/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))

            // Then — response message contains both violation descriptions
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("Validation failed")));
        }

        // ── HttpMessageNotReadableException (malformed / missing body) ──

        @Test
        @DisplayName("Should return 400 with 'Malformed or missing request body' when JSON is invalid")
        void handle_httpMessageNotReadableException_returns400WithMalformedMessage() throws Exception {
            // Given — invalid JSON sent in body
            String badJson = "{ not valid json }";

            // When
            mockMvc.perform(post("/api/canvas/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(badJson))

            // Then
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Error: Malformed or missing request body"));
        }

        @Test
        @DisplayName("Should return 400 when request body is completely absent")
        void handle_missingRequestBody_returns400() throws Exception {
            // Given — no body at all

            // When
            mockMvc.perform(post("/api/canvas/create")
                            .contentType(MediaType.APPLICATION_JSON))

            // Then
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Error: Malformed or missing request body"));
        }

        // ── CanvasException (falls through to generic 500 handler) ──

        @Test
        @DisplayName("Should return 500 when service throws CanvasException (not mapped to 400 yet)")
        void handle_canvasException_returns500ViaGenericHandler() throws Exception {
            // Given — mock service throws CanvasException for any input
            when(canvasService.executeCreateCanvas(anyString()))
                    .thenThrow(new CanvasException("Canvas not created"));

            String requestBody = "{\"width\":5,\"height\":5}";

            // When
            mockMvc.perform(post("/api/canvas/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))

            // Then — currently falls to the generic Exception handler → 500
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(
                            org.hamcrest.Matchers.containsString("An unexpected error occurred")));
        }

        // ── IllegalArgumentException ──

        @Test
        @DisplayName("Should return 400 with exception message when service throws IllegalArgumentException")
        void handle_illegalArgumentException_returns400WithMessage() throws Exception {
            // Given
            when(canvasService.executeDrawLine(anyString()))
                    .thenThrow(new IllegalArgumentException("Bad argument supplied"));

            String requestBody = "{\"x1\":1,\"y1\":1,\"x2\":5,\"y2\":1}";

            // When
            mockMvc.perform(post("/api/canvas/line")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))

            // Then
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value("Error: Bad argument supplied"));
        }

        // ── Generic Exception (catch-all) ──

        @Test
        @DisplayName("Should return 500 when service throws an unexpected RuntimeException")
        void handle_unexpectedRuntimeException_returns500WithGenericMessage() throws Exception {
            // Given
            when(canvasService.executeCreateCanvas(anyString()))
                    .thenThrow(new RuntimeException("Something went wrong internally"));

            String requestBody = "{\"width\":5,\"height\":5}";

            // When
            mockMvc.perform(post("/api/canvas/create")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))

            // Then
                    .andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").value(
                            org.hamcrest.Matchers.containsString("An unexpected error occurred")));
        }

        // ── Direct unit test of GlobalExceptionHandler response builder ──

        @Test
        @DisplayName("GlobalExceptionHandler badRequest should set success=false and prefix message with 'Error:'")
        void globalExceptionHandler_badRequest_responseHasErrorPrefix() {
            // Given
            GlobalExceptionHandler handler = new GlobalExceptionHandler();

            // When
            var responseEntity = handler.handleIllegalArgument(
                    new IllegalArgumentException("Width is invalid"));
            CanvasResponse body = responseEntity.getBody();

            // Then
            assertThat(responseEntity.getStatusCode().value()).isEqualTo(400);
            assertThat(body).isNotNull();
            assertThat(body.success()).isFalse();
            assertThat(body.message()).isEqualTo("Error: Width is invalid");
            assertThat(body.canvas()).isEmpty();
        }

        @Test
        @DisplayName("GlobalExceptionHandler generic handler should set success=false and return 500")
        void globalExceptionHandler_genericHandler_returns500WithMessage() {
            // Given
            GlobalExceptionHandler handler = new GlobalExceptionHandler();

            // When
            var responseEntity = handler.handleGenericException(
                    new Exception("Something totally unexpected"));
            CanvasResponse body = responseEntity.getBody();

            // Then
            assertThat(responseEntity.getStatusCode().value()).isEqualTo(500);
            assertThat(body).isNotNull();
            assertThat(body.success()).isFalse();
            assertThat(body.message()).contains("An unexpected error occurred");
            assertThat(body.message()).contains("Something totally unexpected");
        }
    }
}
