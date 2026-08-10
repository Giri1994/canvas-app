package org.example.canvasbackend.Service;

import org.example.canvasbackend.Exception.CanvasException;
import org.example.canvasbackend.dto.CanvasResponse;

import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Deque;

@Service
public class CanvaServiceImpl implements CanvasService {

    private static final int MAX_CANVAS_SIZE = 1000;
    private static final char EMPTY_CHAR = ' ';
    private static final char DRAW_CHAR = 'x';

    private char[][] canvas;
    private int width;
    private int height;

    @Override
    public synchronized CanvasResponse executeCreateCanvas(String inputVal) {
        String[] parts = parseCommand(inputVal, "C", 3);
        if (parts == null) {
            throw new CanvasException("Invalid create command. Expected: C <width> <height>");
        }

        Integer parsedWidth = parsePositiveInt(parts[1]);
        if (parsedWidth == null) {
            throw new CanvasException("Width must be >= 1");
        }

        Integer parsedHeight = parsePositiveInt(parts[2]);
        if (parsedHeight == null) {
            throw new CanvasException("Height must be >= 1");
        }
        if (parsedWidth > MAX_CANVAS_SIZE || parsedHeight > MAX_CANVAS_SIZE) {
            throw new CanvasException("Width and height must be <= " + MAX_CANVAS_SIZE);
        }

        this.width = parsedWidth;
        this.height = parsedHeight;
        this.canvas = new char[height][width];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                canvas[y][x] = EMPTY_CHAR;
            }
        }

        return ok("Canvas created");
    }

    @Override
    public synchronized CanvasResponse executeDrawLine(String inputVal) {
        if (!hasCanvas()) {
            throw new CanvasException("Canvas not created");
        }

        String[] parts = parseCommand(inputVal, "L", 5);
        if (parts == null) {
            throw new CanvasException("Invalid line command. Expected: L <x1> <y1> <x2> <y2>");
        }

        Integer x1 = parseCoordinate(parts[1]);
        Integer y1 = parseCoordinate(parts[2]);
        Integer x2 = parseCoordinate(parts[3]);
        Integer y2 = parseCoordinate(parts[4]);
        if (x1 == null || y1 == null || x2 == null || y2 == null) {
            throw new CanvasException("Line coordinates must be positive integers");
        }

        if (!isInBounds(x1, y1) || !isInBounds(x2, y2)) {
            throw new CanvasException("Line coordinates are outside canvas bounds");
        }

        if (x1 != x2 && y1 != y2) {
            throw new CanvasException("Only horizontal or vertical lines are supported");
        }

        int startX = Math.min(x1, x2);
        int endX = Math.max(x1, x2);
        int startY = Math.min(y1, y2);
        int endY = Math.max(y1, y2);

        for (int y = startY; y <= endY; y++) {
            for (int x = startX; x <= endX; x++) {
                setCell(x, y, DRAW_CHAR);
            }
        }

        return ok("Line drawn");
    }

    @Override
    public synchronized CanvasResponse executeRectangleLine(String inputVal) {
        if (!hasCanvas()) {
            throw new CanvasException("Canvas not created");
        }

        String[] parts = parseCommand(inputVal, "R", 5);
        if (parts == null) {
            throw new CanvasException("Invalid rectangle command. Expected: R <x1> <y1> <x2> <y2>");
        }

        Integer x1 = parseCoordinate(parts[1]);
        Integer y1 = parseCoordinate(parts[2]);
        Integer x2 = parseCoordinate(parts[3]);
        Integer y2 = parseCoordinate(parts[4]);
        if (x1 == null || y1 == null || x2 == null || y2 == null) {
            throw new CanvasException("Rectangle coordinates must be positive integers");
        }

        if (x1 > x2 || y1 > y2) {
            throw new CanvasException("Rectangle must be specified as x1< x2 & y1 <y2  to the  upper-left (x1,y1) and lower-right (x2,y2)");
        }

        if (!isInBounds(x1, y1) || !isInBounds(x2, y2)) {
            throw new CanvasException("Rectangle coordinates are outside canvas bounds");
        }

        for (int x = x1; x <= x2; x++) {
            setCell(x, y1, DRAW_CHAR);
            setCell(x, y2, DRAW_CHAR);
        }
        for (int y = y1; y <= y2; y++) {
            setCell(x1, y, DRAW_CHAR);
            setCell(x2, y, DRAW_CHAR);
        }

        return ok("Rectangle drawn");
    }

    @Override
    public synchronized CanvasResponse executeBucketFill(String inputValBucketFill) {
        if (!hasCanvas()) {
            throw new CanvasException("Canvas not created");
        }

        String[] parts = parseCommand(inputValBucketFill, "B", 4);
        if (parts == null) {
            throw new CanvasException("Invalid bucket fill command. Expected: B <x> <y> <c>");
        }

        Integer x = parseCoordinate(parts[1]);
        Integer y = parseCoordinate(parts[2]);
        String color = parts[3];

        if (x == null || y == null) {
            throw new CanvasException("Fill coordinates must be positive integers");
        }
        if (!isInBounds(x, y)) {
            throw new CanvasException("Fill coordinates are outside canvas bounds");
        }
        if (color.length() != 1) {
            throw new CanvasException("Color must be a single character");
        }

        char fillChar = color.charAt(0);
        int row = y - 1;
        int col = x - 1;
        char target = canvas[row][col];

        if (target != fillChar) {
            floodFill(col, row, target, fillChar);
        }

        return ok("Fill applied");
    }

    @Override
    public synchronized CanvasResponse executeQuit(String inputValQuit) {
        String[] parts = parseCommand(inputValQuit, "Q", 1);
        if (parts == null) {
            throw new CanvasException("Invalid quit command. Expected: Q");
        }

        this.canvas = null;
        this.width = 0;
        this.height = 0;

        return new CanvasResponse("", "Canvas reset", true);
    }

    private void floodFill(int startCol, int startRow, char target, char replacement) {
        Deque<int[]> queue = new ArrayDeque<>();
        queue.add(new int[]{startRow, startCol});

        while (!queue.isEmpty()) {
            int[] point = queue.removeFirst();
            int row = point[0];
            int col = point[1];

            if (row < 0 || row >= height || col < 0 || col >= width) {
                continue;
            }
            if (canvas[row][col] != target) {
                continue;
            }

            canvas[row][col] = replacement;

            queue.add(new int[]{row - 1, col});
            queue.add(new int[]{row + 1, col});
            queue.add(new int[]{row, col - 1});
            queue.add(new int[]{row, col + 1});
        }
    }

    private boolean hasCanvas() {
        return canvas != null && width > 0 && height > 0;
    }

    private String[] tokenize(String input) {
        return input == null ? new String[0] : input.trim().split("\\s+");
    }

    private String[] parseCommand(String input, String command, int expectedParts) {
        if (input == null || input.isBlank()) {
            return null;
        }
        String[] parts = tokenize(input);
        if (parts.length != expectedParts || !command.equals(parts[0])) {
            return null;
        }
        return parts;
    }

    private Integer parsePositiveInt(String raw) {
        try {
            int value = Integer.parseInt(raw);
            return value >= 1 ? value : null;
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private Integer parseCoordinate(String raw) {
        return parsePositiveInt(raw);
    }

    private boolean isInBounds(int x, int y) {
        return x >= 1 && x <= width && y >= 1 && y <= height;
    }

    private void setCell(int x, int y, char value) {
        canvas[y - 1][x - 1] = value;
    }

    private CanvasResponse ok(String message) {
        return new CanvasResponse(renderCanvas(), message, true);
    }

    private String renderCanvas() {
        if (!hasCanvas()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        String border = "-".repeat(width + 2);

        sb.append(border).append('\n');
        for (int y = 0; y < height; y++) {
            sb.append('|');
            for (int x = 0; x < width; x++) {
                sb.append(canvas[y][x]);
            }
            sb.append('|').append('\n');
        }
        sb.append(border);

        return sb.toString();
    }
}