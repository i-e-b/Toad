package com.ieb.toad.world.core;

import android.graphics.Rect;

import java.io.Serializable;

/** Copy of android.graphics.Rect that implements Serializable */
public class TRect implements Serializable {
    public int left;
    public int top;
    public int right;
    public int bottom;

    public TRect(int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    public TRect() {
        this.left = 0;
        this.top = 0;
        this.right = 0;
        this.bottom = 0;
    }

    public TRect(Rect src) {
        this.left = src.left;
        this.top = src.top;
        this.right = src.right;
        this.bottom = src.bottom;
    }

    public boolean contains(int x, int y) {
        return left < right && top < bottom  // check for empty first
                && x >= left && x < right && y >= top && y < bottom;
    }

    public final int centerX() {
        return (left + right) >> 1;
    }

    public final int centerY() {
        return (top + bottom) >> 1;
    }

    public void set(int left, int top, int right, int bottom) {
        this.left = left;
        this.top = top;
        this.right = right;
        this.bottom = bottom;
    }

    private final transient Rect r = new Rect();
    public Rect rect() {
        r.set(left,top,right,bottom);
        return r;
    }

    public final int width() {
        return right - left;
    }
}