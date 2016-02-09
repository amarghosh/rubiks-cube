package com.amg.rubik;

public final class Constants {
    public static final String CUBE_SIZE = "cube_size";
    public static final String ROTATION_SPEED = "rotation_speed";
    public static final String CUBE_STATE = "cube_state";
    public static final String SCRAMBLE_COUNT = "scramble_count";
    public static final String SCRAMBLE_MODE = "scramble_mode";

    public static int SCR_MODE_INSTANT = 0;
    public static int SCR_MODE_ANIMATED = 1;

    public static int SPEED_SLOW = 0;
    public static int SPEED_MEDIUM = 1;
    public static int SPEED_FAST = 2;

    public static final int DEFAULT_CUBE_SIZE = 3;
    public static final int DEFAULT_SCRAMBLE_COUNT = 4;
    public static final int DEFAULT_SCRAMBLE_MODE_INDEX = SCR_MODE_INSTANT;
    public static final int DEFAULT_SPEED_INDEX = SPEED_MEDIUM;
}
