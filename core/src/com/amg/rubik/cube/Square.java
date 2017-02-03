package com.amg.rubik.cube;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;

import com.amg.rubik.graphics.Axis;
import com.amg.rubik.graphics.Point3D;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;

public class Square {
    private static final String tag = "rubik-square";

    private int mFace;
    private Point3D mCenter;
    private int mColor;
    ModelInstance instance;
    Model model;


    public void setFace(int face) {
        this.mFace = face;
    }

    public int getFace() {
        return mFace;
    }

    public Square(float[] vertices, int color, int face) {
        init(vertices, color, face);
    }

    public Square(float[] vertices, int color) {
        this(vertices, color, -1);
    }

    public Point3D getCenter() {
        return mCenter;
    }

    private Vector3 centerVector = new Vector3();
    private float _radius;

    public Square(float[] vertices) {
        this(vertices, Cube.Color_GRAY);
    }

    private void init(float[] vertices, int color, int face) {
        ModelBuilder builder = new ModelBuilder();
        Material material = new Material(ColorAttribute.createDiffuse(new Color(color)));
        model = builder.createRect(
                vertices[0], vertices[1], vertices[2],
                vertices[3], vertices[4], vertices[5],
                vertices[6], vertices[7], vertices[8],
                vertices[9], vertices[10], vertices[11],
                0, 0, 0, material, VertexAttributes.Usage.Position
        );
        instance = new ModelInstance(model);
        mColor = color;
        mFace = face;
        mCenter = new Point3D();
        mCenter.setX((vertices[0] + vertices[3] + vertices[6] + vertices[9]) / 4);
        mCenter.setY((vertices[1] + vertices[4] + vertices[7] + vertices[10]) / 4);
        mCenter.setZ((vertices[2] + vertices[5] + vertices[8] + vertices[11]) / 4);

        BoundingBox box = new BoundingBox();
        Vector3 dimensions = new Vector3();
        instance.calculateBoundingBox(box);
        box.getCenter(centerVector);
        box.getDimensions(dimensions);
        _radius = dimensions.len() / 2f;
    }

    public Vector3 center() {
        return centerVector;
    }

    public float radius() { return _radius; }

    public Square(Point3D[] points, int color) {
        float[] vertices = new float[points.length * 3]; // x, y, z
        for (int i = 0; i < points.length; i++) {
            vertices[i*3] = points[i].getX();
            vertices[i*3 + 1] = points[i].getY();
            vertices[i*3 + 2] = points[i].getZ();
        }
        init(vertices, color, -1);
    }

    public ModelInstance getModelInstance() {
        return instance;
    }

    public String colorName() {
        return String.format("#%08X",  mColor);
    }

    public int getColor() {
        return mColor;
    }

    public void setColor(int value) {
        if (value == mColor) return;
        mColor = value;
        instance.materials.get(0).set(ColorAttribute.createDiffuse(new Color(value)));
    }

    public void rotateCoordinates(float x, float y, float z, int degrees) {
        instance.transform.setToRotation(x, y, z, degrees);
    }

    public void rotateCoordinates(Axis axis, int angle) {
        int x = 0, y = 0, z = 0;
        switch (axis) {
            case X_AXIS: x = 1; break;
            case Y_AXIS: y = 1; break;
            case Z_AXIS: z = 1; break;
        }
        rotateCoordinates(x, y, z, angle);
    }
}
