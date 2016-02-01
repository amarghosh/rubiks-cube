package com.amg.rubik.cube;

/**
 * Created by amar on 9/12/15.
 */
public interface CubeListener {
    void handleRotationCompleted();
    void handleCubeMessage(String msg);
    void handleCubeSolved();
}
