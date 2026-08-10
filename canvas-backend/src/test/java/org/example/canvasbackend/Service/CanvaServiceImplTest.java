package org.example.canvasbackend.Service;

import org.example.canvasbackend.Exception.CanvasException;
import org.example.canvasbackend.dto.CanvasResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for CanvaServiceImpl.
 *
 * Mockito wires:
 *   @InjectMocks  → creates CanvaServiceImpl and injects any @Mock fields (none here,
 *                   since this service has no external dependencies).
 *
 * Each test follows:
 *   Given  – prepare state / inputs
 *   When   – call the method under test
 *   Then   – assert the result or exception
 */
@ExtendWith(MockitoExtension.class)
class CanvaServiceImplTest {

    // CanvaServiceImpl has no external dependencies, so no @Mock fields are needed.
    // @InjectMocks creates a fresh instance before every test via MockitoExtension.
    @InjectMocks
    private CanvaServiceImpl canvasService;

    // ─────────────────────────────────────────────────────────────
    // executeCreateCanvas
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("executeCreateCanvas")
    class CreateCanvasTests {

        @Test
        @DisplayName("Should create canvas and return success response")
        void createCanvas_validWidthAndHeight_returnsSuccessWithRenderedCanvas() {
            // Given
            String command = "C 5 3";

            // When
            CanvasResponse response = canvasService.executeCreateCanvas(command);

            // Then
            assertThat(response.success()).isTrue();
            assertThat(response.message()).isEqualTo("Canvas created");
            assertThat(response.canvas()).contains("-------");   // width+2 = 7 dashes
            assertThat(response.canvas()).contains("|     |");   // 5 spaces inside
        }

        @Test
        @DisplayName("Should create canvas with minimum allowed size 1x1")
        void createCanvas_minimumDimensions_returnsSuccess() {
            // Given
            String command = "C 1 1";

            // When
            CanvasResponse response = canvasService.executeCreateCanvas(command);

            // Then
            assertThat(response.success()).isTrue();
            assertThat(response.canvas()).contains("---"); // 1 + 2 border dashes
        }

        @Test
        @DisplayName("Should create canvas with maximum allowed size 1000x1000")
        void createCanvas_maximumDimensions_returnsSuccess() {
            // Given
            String command = "C 1000 1000";

            // When
            CanvasResponse response = canvasService.executeCreateCanvas(command);

            // Then
            assertThat(response.success()).isTrue();
            assertThat(response.message()).isEqualTo("Canvas created");
        }

        @Test
        @DisplayName("Should throw CanvasException when width is zero")
        void createCanvas_widthZero_throwsCanvasException() {
            // Given
            String command = "C 0 5";

            // When / Then
            assertThatThrownBy(() -> canvasService.executeCreateCanvas(command))
                    .isInstanceOf(CanvasException.class)
                    .hasMessageContaining("Width must be >= 1");
        }

        @Test
        @DisplayName("Should throw CanvasException when height is zero")
        void createCanvas_heightZero_throwsCanvasException() {
            // Given
            String command = "C 5 0";

            // When / Then
            assertThatThrownBy(() -> canvasService.executeCreateCanvas(command))
                    .isInstanceOf(CanvasException.class)
                    .hasMessageContaining("Height must be >= 1");
        }

        @Test
        @DisplayName("Should throw CanvasException when width exceeds 1000")
        void createCanvas_widthOver1000_throwsCanvasException() {
            // Given
            String command = "C 1001 5";

            // When / Then
            assertThatThrownBy(() -> canvasService.executeCreateCanvas(command))
                    .isInstanceOf(CanvasException.class)
                    .hasMessageContaining("Width and height must be <= 1000");
        }

        @Test
        @DisplayName("Should throw CanvasException when height exceeds 1000")
        void createCanvas_heightOver1000_throwsCanvasException() {
            // Given
            String command = "C 5 1001";

            // When / Then
            assertThatThrownBy(() -> canvasService.executeCreateCanvas(command))
                    .isInstanceOf(CanvasException.class)
                    .hasMessageContaining("Width and height must be <= 1000");
        }

        @Test
        @DisplayName("Should throw CanvasException when command is missing height argument")
        void createCanvas_missingHeightToken_throwsCanvasException() {
            // Given
            String command = "C 5";   // only 2 tokens, expects 3

            // When / Then
            assertThatThrownBy(() -> canvasService.executeCreateCanvas(command))
                    .isInstanceOf(CanvasException.class)
                    .hasMessageContaining("Invalid create command");
        }

        @Test
        @DisplayName("Should throw CanvasException when command prefix is not 'C'")
        void createCanvas_wrongCommandPrefix_throwsCanvasException() {
            // Given
            String command = "X 5 5";

            // When / Then
            assertThatThrownBy(() -> canvasService.executeCreateCanvas(command))
                    .isInstanceOf(CanvasException.class)
                    .hasMessageContaining("Invalid create command");
        }

        @Test
        @DisplayName("Should throw CanvasException when width is not a number")
        void createCanvas_nonNumericWidth_throwsCanvasException() {
            // Given
            String command = "C abc 5";

            // When / Then
            assertThatThrownBy(() -> canvasService.executeCreateCanvas(command))
                    .isInstanceOf(CanvasException.class)
                    .hasMessageContaining("Width must be >= 1");
        }

        @Test
        @DisplayName("Rendered canvas rows should be wrapped in pipe characters")
        void createCanvas_renderedCanvas_rowsHavePipeBorders() {
            // Given
            String command = "C 4 2";

            // When
            CanvasResponse response = canvasService.executeCreateCanvas(command);
            String[] lines = response.canvas().split("\n");

            // Then  — 1 top border + 2 rows + 1 bottom border = 4 lines total
            assertThat(lines).hasSize(4);
            assertThat(lines[1]).startsWith("|").endsWith("|");
            assertThat(lines[2]).startsWith("|").endsWith("|");
        }
    }

    // ─────────────────────────────────────────────────────────────
    // executeDrawLine
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("executeDrawLine")
    class DrawLineTests {

        @BeforeEach
        void givenCanvasOf10x5IsCreated() {
            // Given — a 10-wide, 5-tall canvas exists before each line test
            canvasService.executeCreateCanvas("C 10 5");
        }

        @Test
        @DisplayName("Should draw a horizontal line and mark cells with 'x'")
        void drawLine_horizontalLine_rendersXCharacters() {
            // Given
            String command = "L 1 2 5 2";   // y1 == y2 → horizontal

            // When
            CanvasResponse response = canvasService.executeDrawLine(command);

            // Then
            assertThat(response.success()).isTrue();
            assertThat(response.message()).isEqualTo("Line drawn");
            assertThat(response.canvas()).contains("xxxxx");
        }

        @Test
        @DisplayName("Should draw a vertical line and mark cells with 'x'")
        void drawLine_verticalLine_rendersXCharacters() {
            // Given
            String command = "L 3 1 3 5";   // x1 == x2 → vertical, spans all 5 rows

            // When
            CanvasResponse response = canvasService.executeDrawLine(command);

            // Then
            assertThat(response.success()).isTrue();
            assertThat(response.message()).isEqualTo("Line drawn");
            assertThat(response.canvas().chars().filter(c -> c == 'x').count()).isEqualTo(5);
        }

        @Test
        @DisplayName("Should draw a single-point line when start and end are the same cell")
        void drawLine_singlePoint_rendersOneX() {
            // Given
            String command = "L 2 2 2 2";

            // When
            CanvasResponse response = canvasService.executeDrawLine(command);

            // Then
            assertThat(response.canvas().chars().filter(c -> c == 'x').count()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should throw CanvasException when no canvas has been created")
        void drawLine_noCanvas_throwsCanvasException() {
            // Given — fresh service with no canvas created
            CanvaServiceImpl freshService = new CanvaServiceImpl();
            String command = "L 1 1 5 1";

            // When / Then
            assertThatThrownBy(() -> freshService.executeDrawLine(command))
                    .isInstanceOf(CanvasException.class)
                    .hasMessageContaining("Canvas not created");
        }

        @Test
        @DisplayName("Should throw CanvasException for a diagonal line")
        void drawLine_diagonalLine_throwsCanvasException() {
            // Given
            String command = "L 1 1 4 4";   // x1≠x2 and y1≠y2 → diagonal

            // When / Then
            assertThatThrownBy(() -> canvasService.executeDrawLine(command))
                    .isInstanceOf(CanvasException.class)
                    .hasMessageContaining("Only horizontal or vertical lines are supported");
        }

        @Test
        @DisplayName("Should throw CanvasException when end coordinate exceeds canvas width")
        void drawLine_coordinateOutsideCanvas_throwsCanvasException() {
            // Given
            String command = "L 1 1 15 1";  // x2=15 > width=10

            // When / Then
            assertThatThrownBy(() -> canvasService.executeDrawLine(command))
                    .isInstanceOf(CanvasException.class)
                    .hasMessageContaining("outside canvas bounds");
        }

        @Test
        @DisplayName("Should throw CanvasException when any coordinate is non-positive (0)")
        void drawLine_zeroCoordinate_throwsCanvasException() {
            // Given
            String command = "L 0 1 5 1";  // x1=0 is invalid

            // When / Then
            assertThatThrownBy(() -> canvasService.executeDrawLine(command))
                    .isInstanceOf(CanvasException.class)
                    .hasMessageContaining("must be positive integers");
        }

        @Test
        @DisplayName("Should throw CanvasException when command has missing tokens")
        void drawLine_malformedCommand_throwsCanvasException() {
            // Given
            String command = "L 1 2 3";   // expects 5 tokens, only 4 provided

            // When / Then
            assertThatThrownBy(() -> canvasService.executeDrawLine(command))
                    .isInstanceOf(CanvasException.class)
                    .hasMessageContaining("Invalid line command");
        }
    }

    // ─────────────────────────────────────────────────────────────
    // executeRectangleLine
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("executeRectangleLine")
    class DrawRectangleTests {

        @BeforeEach
        void givenCanvasOf20x10IsCreated() {
            // Given — a 20-wide, 10-tall canvas exists before each rectangle test
            canvasService.executeCreateCanvas("C 20 10");
        }

        @Test
        @DisplayName("Should draw rectangle edges with 'x' for valid coordinates")
        void drawRectangle_validCoordinates_rendersRectangleEdges() {
            // Given
            String command = "R 1 1 5 3";  // 5-wide, 3-tall

            // When
            CanvasResponse response = canvasService.executeRectangleLine(command);

            // Then
            assertThat(response.success()).isTrue();
            assertThat(response.message()).isEqualTo("Rectangle drawn");
            // top+bottom = 5 each, left+right inner = 1 each → 12 x-chars total
            assertThat(response.canvas().chars().filter(c -> c == 'x').count()).isEqualTo(12);
        }

        @Test
        @DisplayName("Should draw a 1x1 rectangle as a single 'x' cell")
        void drawRectangle_singlePoint_rendersOneX() {
            // Given
            String command = "R 3 3 3 3";

            // When
            CanvasResponse response = canvasService.executeRectangleLine(command);

            // Then
            assertThat(response.canvas().chars().filter(c -> c == 'x').count()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should reject reversed coordinates because the spec requires upper-left to lower-right order")
        void drawRectangle_reversedCorners_throwsCanvasException() {
            // Given — x2 < x1 and y2 < y1 violate the assignment contract
            String command = "R 5 3 1 1";

            // When / Then
            assertThatThrownBy(() -> canvasService.executeRectangleLine(command))
                    .isInstanceOf(CanvasException.class)
                    .hasMessageContaining("upper-left (x1,y1) and lower-right (x2,y2)");
        }

        @Test
        @DisplayName("Should throw CanvasException when no canvas has been created")
        void drawRectangle_noCanvas_throwsCanvasException() {
            // Given — fresh service with no canvas
            CanvaServiceImpl freshService = new CanvaServiceImpl();
            String command = "R 1 1 4 3";

            // When / Then
            assertThatThrownBy(() -> freshService.executeRectangleLine(command))
                    .isInstanceOf(CanvasException.class)
                    .hasMessageContaining("Canvas not created");
        }

        @Test
        @DisplayName("Should throw CanvasException when rectangle goes outside canvas bounds")
        void drawRectangle_coordinatesOutOfBounds_throwsCanvasException() {
            // Given
            String command = "R 1 1 25 10";  // x2=25 > width=20

            // When / Then
            assertThatThrownBy(() -> canvasService.executeRectangleLine(command))
                    .isInstanceOf(CanvasException.class)
                    .hasMessageContaining("outside canvas bounds");
        }

        @Test
        @DisplayName("Should throw CanvasException when command has missing tokens")
        void drawRectangle_malformedCommand_throwsCanvasException() {
            // Given
            String command = "R 1 2 3";  // 4 tokens, expects 5

            // When / Then
            assertThatThrownBy(() -> canvasService.executeRectangleLine(command))
                    .isInstanceOf(CanvasException.class)
                    .hasMessageContaining("Invalid rectangle command");
        }
    }

    // ─────────────────────────────────────────────────────────────
    // executeBucketFill
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("executeBucketFill")
    class BucketFillTests {

        @BeforeEach
        void givenCanvasWithVerticalDivider() {
            // Given — 10x5 canvas with a vertical divider line at x=5
            canvasService.executeCreateCanvas("C 10 5");
            canvasService.executeDrawLine("L 5 1 5 5");
        }

        @Test
        @DisplayName("Should flood-fill the left region with the given color")
        void bucketFill_emptyRegion_fillsAllConnectedCells() {
            // Given
            String command = "B 1 1 o";   // fill left side with 'o'

            // When
            CanvasResponse response = canvasService.executeBucketFill(command);

            // Then
            assertThat(response.success()).isTrue();
            assertThat(response.message()).isEqualTo("Fill applied");
            assertThat(response.canvas()).contains("o");
            // Right region (x=6..10) remains empty space
            assertThat(response.canvas()).contains(" ");
        }

        @Test
        @DisplayName("Should not spread fill across the divider line ('x' boundary)")
        void bucketFill_fillStopsAtLineBoundary_rightSideUnchanged() {
            // Given
            String command = "B 1 1 o";   // fill left of divider

            // When
            CanvasResponse response = canvasService.executeBucketFill(command);
            String canvas = response.canvas();

            // Then — right side must still have spaces, not 'o'
            // Count 'o' chars: left region = 4 cols × 5 rows = 20
            assertThat(canvas.chars().filter(c -> c == 'o').count()).isEqualTo(20);
        }

        @Test
        @DisplayName("Should not change canvas when fill color equals current cell color")
        void bucketFill_targetEqualsFillColor_canvasUnchanged() {
            // Given — fill left region with 'o', then re-fill same point with 'o'
            canvasService.executeBucketFill("B 1 1 o");
            long xCountBefore = canvasService
                    .executeBucketFill("B 1 1 o")
                    .canvas().chars().filter(c -> c == 'o').count();

            // When — repeat fill (target == fillChar → no-op)
            CanvasResponse response = canvasService.executeBucketFill("B 1 1 o");
            long xCountAfter = response.canvas().chars().filter(c -> c == 'o').count();

            // Then — count of 'o' does not change
            assertThat(xCountAfter).isEqualTo(xCountBefore);
        }

        @Test
        @DisplayName("Should throw CanvasException when no canvas has been created")
        void bucketFill_noCanvas_throwsCanvasException() {
            // Given — fresh service, no canvas
            CanvaServiceImpl freshService = new CanvaServiceImpl();
            String command = "B 1 1 o";

            // When / Then
            assertThatThrownBy(() -> freshService.executeBucketFill(command))
                    .isInstanceOf(CanvasException.class)
                    .hasMessageContaining("Canvas not created");
        }

        @Test
        @DisplayName("Should throw CanvasException when x coordinate is outside canvas")
        void bucketFill_coordinateOutOfBounds_throwsCanvasException() {
            // Given
            String command = "B 15 1 o";   // x=15 > width=10

            // When / Then
            assertThatThrownBy(() -> canvasService.executeBucketFill(command))
                    .isInstanceOf(CanvasException.class)
                    .hasMessageContaining("outside canvas bounds");
        }

        @Test
        @DisplayName("Should throw CanvasException when color is more than one character")
        void bucketFill_multiCharColor_throwsCanvasException() {
            // Given
            String command = "B 1 1 oo";  // 'oo' is not a single character

            // When / Then
            assertThatThrownBy(() -> canvasService.executeBucketFill(command))
                    .isInstanceOf(CanvasException.class)
                    .hasMessageContaining("Color must be a single character");
        }

        @Test
        @DisplayName("Should throw CanvasException when color argument is missing")
        void bucketFill_missingColorToken_throwsCanvasException() {
            // Given
            String command = "B 1 1";   // 3 tokens, expects 4

            // When / Then
            assertThatThrownBy(() -> canvasService.executeBucketFill(command))
                    .isInstanceOf(CanvasException.class)
                    .hasMessageContaining("Invalid bucket fill command");
        }
    }

    // ─────────────────────────────────────────────────────────────
    // executeQuit
    // ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("executeQuit")
    class QuitTests {

        @Test
        @DisplayName("Should reset canvas and return success response")
        void quit_canvasExists_resetsCanvasState() {
            // Given
            canvasService.executeCreateCanvas("C 5 5");

            // When
            CanvasResponse response = canvasService.executeQuit("Q");

            // Then
            assertThat(response.success()).isTrue();
            assertThat(response.message()).isEqualTo("Canvas reset");
            assertThat(response.canvas()).isEmpty();
        }

        @Test
        @DisplayName("Should allow creating a fresh canvas after quit")
        void quit_thenCreateCanvas_createsNewCanvasSuccessfully() {
            // Given
            canvasService.executeCreateCanvas("C 5 5");
            canvasService.executeQuit("Q");

            // When
            CanvasResponse response = canvasService.executeCreateCanvas("C 3 2");

            // Then
            assertThat(response.success()).isTrue();
            assertThat(response.message()).isEqualTo("Canvas created");
        }

        @Test
        @DisplayName("Should throw CanvasException when drawing a line after quit")
        void quit_drawLineAfterQuit_throwsCanvasException() {
            // Given
            canvasService.executeCreateCanvas("C 5 5");
            canvasService.executeQuit("Q");

            // When / Then
            assertThatThrownBy(() -> canvasService.executeDrawLine("L 1 1 3 1"))
                    .isInstanceOf(CanvasException.class)
                    .hasMessageContaining("Canvas not created");
        }

        @Test
        @DisplayName("Should throw CanvasException when quit command has extra tokens")
        void quit_extraTokensInCommand_throwsCanvasException() {
            // Given
            String command = "Q extra";   // expects exactly 1 token

            // When / Then
            assertThatThrownBy(() -> canvasService.executeQuit(command))
                    .isInstanceOf(CanvasException.class)
                    .hasMessageContaining("Invalid quit command");
        }
    }
}
