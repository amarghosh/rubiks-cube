package com.amg.rubik;

/**
 * Created by amar on 9/12/15.
 */
public class RubiksCube3x3x3 extends RubiksCube {

    public RubiksCube3x3x3() {
        super(3);
    }

    @Override
    public int solve() {
        if (mState != CubeState.IDLE) {
            sendMessage("Invalid state to solve: " + mState);
            return -1;
        }
        mState = CubeState.SOLVING;
        return 0;
    }
}
