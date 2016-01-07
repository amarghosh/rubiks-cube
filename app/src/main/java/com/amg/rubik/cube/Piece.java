package com.amg.rubik.cube;

import android.util.Log;

import java.util.ArrayList;

/**
 * Created by amar on 30/11/15.
 */
public class Piece {

    public enum PieceType {
        CORNER,
        CENTER,
        EDGE
    }

    protected ArrayList<Square> mSquares;
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

    protected void addSquare(Square sq) {
        if (mType == PieceType.CENTER && mSquares.size() != 0 ||
                mType == PieceType.EDGE && mSquares.size() >= 2 ||
                mType == PieceType.CORNER && mSquares.size() >= 3) {
            Log.w("piece", "Too many squares for PieceType " + mType +
                    ", we have " + mSquares.size());
        }
        mSquares.add(sq);
    }

    public boolean hasColor(int color) {
        for (Square sq: mSquares) {
            if (sq.mColor == color) {
                return true;
            }
        }
        return false;
    }

    public Square getSquare(int color) {
        for (Square sq: mSquares) {
            if (sq.mColor == color) {
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
