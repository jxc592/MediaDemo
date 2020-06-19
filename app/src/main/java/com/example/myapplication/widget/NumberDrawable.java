package com.example.myapplication.widget;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class NumberDrawable extends Drawable {
    int number;
    boolean state;

    Paint mPaint;
    public NumberDrawable(int number) {
        this(number,false);
    }

    public NumberDrawable(int number, boolean state) {
        this.number = number;
        this.state = state;
        mPaint = new Paint();
        mPaint.setColor(Color.LTGRAY);
        mPaint.setStrokeWidth(10);
        if(state) {
            mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            mPaint.setTextLocale(Locale.getDefault());
            mPaint.setSubpixelText(true);
        } else {
            mPaint.setStyle(Paint.Style.STROKE);
        }
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        canvas.drawRoundRect(0f,0f,canvas.getWidth(),canvas.getHeight(),90f,90f,mPaint);
        canvas.drawText(String.valueOf(number),canvas.getWidth()/2,canvas.getHeight()/2,mPaint);
    }

    @Override
    public void setAlpha(int alpha) {

    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {

    }

    @Override
    public int getOpacity() {
        return 0;
    }
}
