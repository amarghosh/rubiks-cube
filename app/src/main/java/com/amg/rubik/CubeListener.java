package com.amg.rubik;

/**
 * Created by amar on 9/12/15.
 */
public interface CubeListener {
    public void handleCubeMessage(String msg);
    public void handleCubeSolved();
}
