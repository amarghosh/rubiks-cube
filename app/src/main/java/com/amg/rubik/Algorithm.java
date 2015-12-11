package com.amg.rubik;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by amar on 11/12/15.
 */
public class Algorithm {

    private static final String tag = "algo";

    private ArrayList<Rotation> steps;
    private int currentPosition;

    public Algorithm() {
        steps = new ArrayList<>();
        reset();
    }

    private void reset() {
        steps.clear();
        currentPosition = 0;
    }

    public void addStep(Rotation rotation) {
        Log.w(tag, rotation.toString());
        steps.add(rotation);
    }

    public boolean isDone() {
        return currentPosition >= steps.size();
    }

    public Rotation getNextStep() {
        if (currentPosition >= steps.size()) {
            return null;
        }
        return steps.get(currentPosition++).duplicate();
    }

    public static Algorithm rotateWhole (Rotation.Axis axis, Rotation.Direction direction,
                                         int cubeSize, int count) {
        Algorithm algo = new Algorithm();
        for (int i = 0; i < count; i++) {
            Rotation rot = new Rotation(axis, direction, 0, cubeSize);
            algo.addStep(rot);
        }
        return algo;
    }
}
