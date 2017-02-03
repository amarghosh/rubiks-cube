package com.amg.rubik;

import com.amg.rubik.cube.Piece;
import com.amg.rubik.cube.RubiksCube;
import com.amg.rubik.cube.Square;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;

import java.util.ArrayList;

/**
 * Created by amar on 3/2/17.
 */

public class InputHandler extends InputAdapter {
    private static final String tag = "rubik-touch";
    RubiksCube cube;
    Camera camera;
    int touchStartIndex = -1;
    int touchDragIndex = -1;
    int touchStartX;
    int touchStartY;

    public InputHandler(RubiksCube cube, Camera camera) {
        this.cube = cube;
        this.camera = camera;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        touchStartIndex = getObject(screenX, screenY);
        if (touchStartIndex >= 0) {
            touchStartX = screenX;
            touchStartY = screenY;
            touchDragIndex = touchStartIndex;
        }
        return touchStartIndex >= 0;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (touchStartIndex < 0) return false;
        int current = getObject(screenX, screenY);
        if (current >= 0) touchDragIndex = current;
        Log.w(tag, String.format("Touch: %d, %d", touchStartIndex, touchDragIndex));
        cube.tryRotate(touchStartIndex, touchDragIndex);
        touchStartIndex = -1;
        touchDragIndex = -1;
        return true;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (touchStartIndex == -1) return false;
        int index = getObject(screenX, screenY);
        if (index >= 0) touchDragIndex = index;
        return true;
    }

    public int getObject(int x, int y) {
        Vector3 position = new Vector3();
        Ray ray = camera.getPickRay(x, y);
        int result = -1;
        float distance = -1;
        ArrayList<Square> squares = cube.getSquares();
        for (int i = 0; i < squares.size(); i++) {
            final Square square = squares.get(i);
            final ModelInstance inst = square.getModelInstance();
            inst.transform.getTranslation(position);
            position.add(square.center());
            float dist2 = ray.origin.dst2(position);
            if (distance > 0 && dist2 > distance)
                continue;
            if (Intersector.intersectRaySphere(ray, position, square.radius(), null)) {
                result = i;
                distance = dist2;
            }
        }
        return result;
    }
}
