package com.amg.rubik.cube;

import android.util.Log;

import java.util.ArrayList;

public class Piece {

    private static final String tag = "rubik-piece";

    public enum PieceType {
        CORNER,
        CENTER,
        EDGE
    }

    ArrayList<Square> mSquares;
    private PieceType mType;

    public Piece(PieceType type) {
        mSquares = new ArrayList<>();
        mType = type;
    }

    public Piece(Square center) {
        mSquares = new ArrayList<>();
        mType = PieceType.CENTER;
        mSquares.add(center);
    }

    public PieceType getType() {
        return mType;
    }

    void addSquare(Square sq) {
        if (mSquares.contains(sq)) {
            return;
        }
        if (mType == PieceType.CENTER && mSquares.size() >= 2 ||
                mType == PieceType.EDGE && mSquares.size() >= 3 ||
                mType == PieceType.CORNER && mSquares.size() >= 6) {
            Log.e(tag, "Too many squares for PieceType " + mType +
                    ", we already have " + mSquares.size());
        }
        mSquares.add(sq);
    }

    public boolean hasColor(int color) {
        for (Square sq: mSquares) {
            if (sq.getColor() == color) {
                return true;
            }
        }
        return false;
    }

    public Square getSquare(int color) {
        for (Square sq: mSquares) {
            if (sq.getColor() == color) {
                return sq;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        String s = mSquares.get(0).colorName();
        for (int i = 1; i < mSquares.size(); i++) {
            s += "-" + mSquares.get(i).colorName();
        }
        return s;
    }
}
