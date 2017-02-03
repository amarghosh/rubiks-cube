package com.amg.rubik.graphics;

import com.amg.rubik.cube.Square;

public interface CubeRenderer {
    void drawSquare(Square square);

    /**
     * Rotation is a property of individual pieces (or the cube as a whole) and ideally
     * it should be in Square or Piece class. But then we would have to recalculate the rotation
     * matrix before drawing every square. Hence its put in Renderer
     * */
    void setRotation(float angle, float x, float y, float z);
}
