package com.amg.rubik.ui;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.amg.rubik.R;

/**
 * Horizontal number picker with increment and decrement buttons around the number field.
 */
public class NumberPicker extends FrameLayout {
    private static final String tag = "numpicker";

    private int mIncrementColor = Color.BLUE;
    private int mDecrementColor = Color.BLUE;
    private int mMinValue = 0;
    private int mMaxValue = 100;
    private int mStep = 1;
    private int mValue = 1;

    private TextView textView;

    public NumberPicker(Context context) {
        super(context);
        init(null, 0);
    }

    public NumberPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public NumberPicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.NumberPicker, defStyle, 0);

        mIncrementColor = a.getColor(R.styleable.NumberPicker_incrementColor, mIncrementColor);
        mDecrementColor = a.getColor(R.styleable.NumberPicker_decrementColor, mDecrementColor);
        mMinValue = a.getInteger(R.styleable.NumberPicker_minValue, mMinValue);
        mMaxValue = a.getInteger(R.styleable.NumberPicker_maxValue, mMaxValue);
        mStep = a.getInteger(R.styleable.NumberPicker_step, mStep);
        mValue = a.getInteger(R.styleable.NumberPicker_value, mValue);

        Log.w(tag, String.format("inited with colors %d %d, range %d %d, step %d, value %d ",
                mIncrementColor, mDecrementColor,
                mMinValue, mMaxValue, mStep, mValue
        ));

        a.recycle();

        View view = inflate(getContext(), R.layout.number_picker_layout, null);
        addView(view);

        textView = (TextView)findViewById(R.id.numberField);
        textView.setText(String.valueOf(mValue));
    }

    /**
     * Gets the increment button's color
     *
     * @return The color attribute value.
     */
    public int getIncrementColor() {
        return mIncrementColor;
    }

    /**
     * Sets the increment button's color
     *
     * @param color The color attribute value to use.
     */
    public void setIncrementColor(int color) {
        mIncrementColor = color;
    }

    /**
     * Gets the decrement button's color
     *
     * @return The color attribute value.
     */
    public int getDecrementColor() {
        return mDecrementColor;
    }

    /**
     * Sets the decrement button's color
     *
     * @param color The color attribute value to use.
     */
    public void setDecrementColor(int color) {
        this.mDecrementColor = mDecrementColor;
    }

    public int getMinValue() {
        return mMinValue;
    }

    public void setMinValue(int minValue) {
        this.mMinValue = minValue;
    }

    public int getMaxValue() {
        return mMaxValue;
    }

    public void setMaxValue(int maxValue) {
        this.mMaxValue = maxValue;
    }

    public int getStep() {
        return mStep;
    }

    public void setStep(int step) {
        this.mStep = step;
    }

    public int getValue() {
        return mValue;
    }

    public void setValue(int value) {
        this.mValue = value;
    }
}
