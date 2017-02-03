package com.amg.rubik.cube;

import com.amg.rubik.Log;
import com.amg.rubik.graphics.Axis;
import com.amg.rubik.graphics.Direction;

import java.util.ArrayList;

public class Algorithm {

    private static final String tag = "rubik-algo";

    private ArrayList<Rotation> steps;
    private int currentPosition;

    public Algorithm() {
        steps = new ArrayList<>();
        reset();
    }

    public Algorithm(ArrayList<Rotation> rotations) {
        this();
        for (Rotation rot: rotations) {
            addStep(rot);
        }
    }

    private void reset() {
        steps.clear();
        currentPosition = 0;
    }

    public void addStep(Axis axis, Direction direction, int face, int faceCount) {
        addStep(new Rotation(axis, direction, face, faceCount));
    }

    public void addStep(Axis axis, Direction direction, int face) {
        addStep(new Rotation(axis, direction, face));
    }

    public void addStep(Rotation rotation) {
        steps.add(rotation);
    }

    public void append(Algorithm algo) {
        if (algo == null) return;
        for (Rotation rot: algo.steps) {
            addStep(rot.duplicate());
        }
    }

    public void repeatLastStep() {
        addStep(steps.get(steps.size() - 1).duplicate());
    }

    public boolean isDone() {
        return currentPosition >= steps.size();
    }

    public Rotation getNextStep() {
        if (currentPosition >= steps.size()) {
            Log.w(tag, "No more steps: " + currentPosition + ", " + steps.size());
            return null;
        }
        return steps.get(currentPosition++).duplicate();
    }

    public static Algorithm rotateWhole (Axis axis, Direction direction,
                                         int cubeSize, int count) {
        Algorithm algo = new Algorithm();
        for (int i = 0; i < count; i++) {
            Rotation rot = new Rotation(axis, direction, 0, cubeSize);
            algo.addStep(rot);
        }
        return algo;
    }
}
