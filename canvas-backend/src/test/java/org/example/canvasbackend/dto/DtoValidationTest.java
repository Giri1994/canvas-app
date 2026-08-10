package org.example.canvasbackend.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for all DTO classes.
 *
 * Strategy:
 *   - CanvasResponse (record)    → test field values via record accessors
 *   - CreateCanvasRequest        → test @Min / @Max Bean Validation constraints
 *   - LineRequest                → test @Min constraints on all four coordinates
 *   - RectangleRequest           → test @Min constraints on all four coordinates
 *   - FillRequest                → test @Min, @NotBlank, @Size constraints
 *
 * No Mockito needed here — pure unit tests using Jakarta Bean Validation directly.
 *
 * Each test follows:
 *   Given  → build the DTO object with specific values
 *   When   → run the validator
 *   Then   → assert violations (or absence of them)
 */
class DtoValidationTest {

    private static Validator validator;

    @BeforeAll
    static void setUpValidator() {
        // Given — create a Jakarta Bean Validation validator (same engine Spring uses)
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    // helper: collect all violation messages into a single string for easy assertion
    private <T> Set<String> violationMessages(T dto) {
        return validator.validate(dto)
                .stream()
                .map(ConstraintViolation::getMessage)
                .collect(Collectors.toSet());
    }

    // ─────────────────────────────────────────────────────────────
    // CanvasResponse  (Java record)
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("CanvasResponse")
    class CanvasResponseTests {

        @Test
        @DisplayName("Should store all three fields accessible via record accessors")
        void canvasResponse_allFields_accessibleViaAccessors() {
            // Given
            String canvasContent = "---\n| |\n---";
            String message       = "Canvas created";
            boolean success      = true;

            // When
            CanvasResponse response = new CanvasResponse(canvasContent, message, success);

            // Then
            assertThat(response.canvas()).isEqualTo(canvasContent);
            assertThat(response.message()).isEqualTo(message);
            assertThat(response.success()).isTrue();
        }

        @Test
        @DisplayName("Should allow empty canvas string and false success for error responses")
        void canvasResponse_errorResponse_allowsEmptyCanvasAndFalse() {
            // Given / When
            CanvasResponse response = new CanvasResponse("", "Error: Canvas not created", false);

            // Then
            assertThat(response.canvas()).isEmpty();
            assertThat(response.success()).isFalse();
            assertThat(response.message()).startsWith("Error:");
        }

        @Test
        @DisplayName("Two records with same values should be equal (value semantics)")
        void canvasResponse_sameValues_areEqual() {
            // Given
            CanvasResponse r1 = new CanvasResponse("abc", "ok", true);
            CanvasResponse r2 = new CanvasResponse("abc", "ok", true);

            // When / Then
            assertThat(r1).isEqualTo(r2);
            assertThat(r1.hashCode()).isEqualTo(r2.hashCode());
        }

        @Test
        @DisplayName("Two records with different values should not be equal")
        void canvasResponse_differentValues_areNotEqual() {
            // Given
            CanvasResponse r1 = new CanvasResponse("canvas1", "ok", true);
            CanvasResponse r2 = new CanvasResponse("canvas2", "ok", true);

            // When / Then
            assertThat(r1).isNotEqualTo(r2);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // CreateCanvasRequest
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("CreateCanvasRequest")
    class CreateCanvasRequestTests {

        @Test
        @DisplayName("Should pass validation with width=1 and height=1 (minimum boundary)")
        void createCanvasRequest_minimumValidValues_noViolations() {
            // Given
            CreateCanvasRequest request = new CreateCanvasRequest();
            request.setWidth(1);
            request.setHeight(1);

            // When
            Set<String> violations = violationMessages(request);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should pass validation with width=1000 and height=1000 (maximum boundary)")
        void createCanvasRequest_maximumValidValues_noViolations() {
            // Given
            CreateCanvasRequest request = new CreateCanvasRequest();
            request.setWidth(1000);
            request.setHeight(1000);

            // When
            Set<String> violations = violationMessages(request);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should fail validation when width is 0 (below @Min(1))")
        void createCanvasRequest_widthZero_violatesMinConstraint() {
            // Given
            CreateCanvasRequest request = new CreateCanvasRequest();
            request.setWidth(0);
            request.setHeight(5);

            // When
            Set<String> violations = violationMessages(request);

            // Then
            assertThat(violations).contains("Width must be >= 1");
        }

        @Test
        @DisplayName("Should fail validation when height is 0 (below @Min(1))")
        void createCanvasRequest_heightZero_violatesMinConstraint() {
            // Given
            CreateCanvasRequest request = new CreateCanvasRequest();
            request.setWidth(5);
            request.setHeight(0);

            // When
            Set<String> violations = violationMessages(request);

            // Then
            assertThat(violations).contains("Height must be >= 1");
        }

        @Test
        @DisplayName("Should fail validation when width is 1001 (exceeds @Max(1000))")
        void createCanvasRequest_widthOver1000_violatesMaxConstraint() {
            // Given
            CreateCanvasRequest request = new CreateCanvasRequest();
            request.setWidth(1001);
            request.setHeight(5);

            // When
            Set<String> violations = violationMessages(request);

            // Then
            assertThat(violations).contains("Width must be <= 1000");
        }

        @Test
        @DisplayName("Should fail validation when height is 1001 (exceeds @Max(1000))")
        void createCanvasRequest_heightOver1000_violatesMaxConstraint() {
            // Given
            CreateCanvasRequest request = new CreateCanvasRequest();
            request.setWidth(5);
            request.setHeight(1001);

            // When
            Set<String> violations = violationMessages(request);

            // Then
            assertThat(violations).contains("Height must be <= 1000");
        }

        @Test
        @DisplayName("Should produce two violations when both width and height are invalid")
        void createCanvasRequest_bothInvalid_twoViolations() {
            // Given
            CreateCanvasRequest request = new CreateCanvasRequest();
            request.setWidth(0);
            request.setHeight(0);

            // When
            Set<ConstraintViolation<CreateCanvasRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(2);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // LineRequest
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("LineRequest")
    class LineRequestTests {

        @Test
        @DisplayName("Should pass validation when all coordinates are >= 1")
        void lineRequest_allCoordsPositive_noViolations() {
            // Given
            LineRequest request = new LineRequest();
            request.setX1(1); request.setY1(1);
            request.setX2(5); request.setY2(3);

            // When
            Set<String> violations = violationMessages(request);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should fail validation when x1 is 0")
        void lineRequest_x1Zero_violatesMinConstraint() {
            // Given
            LineRequest request = new LineRequest();
            request.setX1(0); request.setY1(1);
            request.setX2(5); request.setY2(1);

            // When
            Set<String> violations = violationMessages(request);

            // Then
            assertThat(violations).contains("x1 must be >= 1");
        }

        @Test
        @DisplayName("Should fail validation when y1 is 0")
        void lineRequest_y1Zero_violatesMinConstraint() {
            // Given
            LineRequest request = new LineRequest();
            request.setX1(1); request.setY1(0);
            request.setX2(5); request.setY2(1);

            // When
            Set<String> violations = violationMessages(request);

            // Then
            assertThat(violations).contains("y1 must be >= 1");
        }

        @Test
        @DisplayName("Should fail validation when x2 is 0")
        void lineRequest_x2Zero_violatesMinConstraint() {
            // Given
            LineRequest request = new LineRequest();
            request.setX1(1); request.setY1(1);
            request.setX2(0); request.setY2(1);

            // When
            Set<String> violations = violationMessages(request);

            // Then
            assertThat(violations).contains("x2 must be >= 1");
        }

        @Test
        @DisplayName("Should fail validation when y2 is 0")
        void lineRequest_y2Zero_violatesMinConstraint() {
            // Given
            LineRequest request = new LineRequest();
            request.setX1(1); request.setY1(1);
            request.setX2(5); request.setY2(0);

            // When
            Set<String> violations = violationMessages(request);

            // Then
            assertThat(violations).contains("y2 must be >= 1");
        }

        @Test
        @DisplayName("Should produce four violations when all coordinates are 0")
        void lineRequest_allZero_fourViolations() {
            // Given
            LineRequest request = new LineRequest();
            // all int fields default to 0, which violates @Min(1)

            // When
            Set<ConstraintViolation<LineRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(4);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // RectangleRequest
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("RectangleRequest")
    class RectangleRequestTests {

        @Test
        @DisplayName("Should pass validation when all coordinates are >= 1")
        void rectangleRequest_allCoordsPositive_noViolations() {
            // Given
            RectangleRequest request = new RectangleRequest();
            request.setX1(1); request.setY1(1);
            request.setX2(10); request.setY2(5);

            // When
            Set<String> violations = violationMessages(request);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should fail validation when x1 is 0")
        void rectangleRequest_x1Zero_violatesMinConstraint() {
            // Given
            RectangleRequest request = new RectangleRequest();
            request.setX1(0); request.setY1(1);
            request.setX2(5); request.setY2(3);

            // When
            Set<String> violations = violationMessages(request);

            // Then
            assertThat(violations).contains("x1 must be >= 1");
        }

        @Test
        @DisplayName("Should fail validation when y2 is 0")
        void rectangleRequest_y2Zero_violatesMinConstraint() {
            // Given
            RectangleRequest request = new RectangleRequest();
            request.setX1(1); request.setY1(1);
            request.setX2(5); request.setY2(0);

            // When
            Set<String> violations = violationMessages(request);

            // Then
            assertThat(violations).contains("y2 must be >= 1");
        }

        @Test
        @DisplayName("Should produce four violations when all coordinates are 0")
        void rectangleRequest_allZero_fourViolations() {
            // Given — all int fields default to 0, violating @Min(1) on each
            RectangleRequest request = new RectangleRequest();

            // When
            Set<ConstraintViolation<RectangleRequest>> violations = validator.validate(request);

            // Then
            assertThat(violations).hasSize(4);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // FillRequest
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("FillRequest")
    class FillRequestTests {

        @Test
        @DisplayName("Should pass validation with x=1, y=1 and a single-character color")
        void fillRequest_validValues_noViolations() {
            // Given
            FillRequest request = new FillRequest();
            request.setX(1);
            request.setY(1);
            request.setColor("o");

            // When
            Set<String> violations = violationMessages(request);

            // Then
            assertThat(violations).isEmpty();
        }

        @Test
        @DisplayName("Should fail validation when x is 0 (below @Min(1))")
        void fillRequest_xZero_violatesMinConstraint() {
            // Given
            FillRequest request = new FillRequest();
            request.setX(0); request.setY(1); request.setColor("o");

            // When
            Set<String> violations = violationMessages(request);

            // Then
            assertThat(violations).contains("x must be >= 1");
        }

        @Test
        @DisplayName("Should fail validation when y is 0 (below @Min(1))")
        void fillRequest_yZero_violatesMinConstraint() {
            // Given
            FillRequest request = new FillRequest();
            request.setX(1); request.setY(0); request.setColor("o");

            // When
            Set<String> violations = violationMessages(request);

            // Then
            assertThat(violations).contains("y must be >= 1");
        }

        @Test
        @DisplayName("Should fail validation when color is blank (@NotBlank)")
        void fillRequest_blankColor_violatesNotBlankConstraint() {
            // Given
            FillRequest request = new FillRequest();
            request.setX(1); request.setY(1); request.setColor("   ");  // blank string

            // When
            Set<String> violations = violationMessages(request);

            // Then
            assertThat(violations).contains("color is required");
        }

        @Test
        @DisplayName("Should fail validation when color is empty string (@NotBlank)")
        void fillRequest_emptyColor_violatesNotBlankConstraint() {
            // Given
            FillRequest request = new FillRequest();
            request.setX(1); request.setY(1); request.setColor("");

            // When
            Set<String> violations = violationMessages(request);

            // Then
            assertThat(violations).anyMatch(v -> v.contains("color"));
        }

        @Test
        @DisplayName("Should fail validation when color has more than one character (@Size max=1)")
        void fillRequest_multiCharColor_violatesSizeConstraint() {
            // Given
            FillRequest request = new FillRequest();
            request.setX(1); request.setY(1); request.setColor("oo");

            // When
            Set<String> violations = violationMessages(request);

            // Then
            assertThat(violations).contains("color must be a single character");
        }

        @Test
        @DisplayName("Should fail with multiple violations when x, y, and color are all invalid")
        void fillRequest_allInvalid_multipleViolations() {
            // Given
            FillRequest request = new FillRequest();
            request.setX(0); request.setY(0); request.setColor("ab");

            // When
            Set<ConstraintViolation<FillRequest>> violations = validator.validate(request);

            // Then — at least 3 violations: x, y, color size
            assertThat(violations.size()).isGreaterThanOrEqualTo(3);
        }
    }
}
