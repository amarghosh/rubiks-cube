package com.amg.rubik.cube;

public interface CubeListener {
    void handleRotationCompleted();
    void handleCubeMessage(String msg);
    void handleCubeSolved();
}
