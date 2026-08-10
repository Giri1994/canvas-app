package org.example.canvasbackend.Service;

import org.example.canvasbackend.dto.CanvasResponse;

public interface CanvasService {
    CanvasResponse executeCreateCanvas(String inputVal);

    CanvasResponse executeDrawLine(String inputVal);

    CanvasResponse executeRectangleLine(String inputVal);

    CanvasResponse executeBucketFill(String inputValBucketFill);

    CanvasResponse executeQuit(String inputValQuit);
}
