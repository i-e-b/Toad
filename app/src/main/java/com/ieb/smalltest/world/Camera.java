package com.ieb.smalltest.world;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import org.jetbrains.annotations.NotNull;

/** Helper to draw on a canvas with an offset */
public class Camera {
    private Canvas canvas;
    private int dx,dy;
    private Rect box;
    private int width, height;
    private int drawCount;

    /** Set the canvas to draw into */
    public void use(@NotNull Canvas canvas) {
        this.canvas = canvas;
        width = canvas.getWidth();
        height = canvas.getHeight();
        dx = 0;
        dy = 0;
        box = new Rect();
    }

    public void resetCount(){
        drawCount = 0;
    }

    public int getCount(){
        return drawCount;
    }

    /** Set the camera offset */
    public void centreOn(double x, double y) {
        dx = (int) x - (width / 2);
        dy = (int) y - (height / 2);
    }

    // TODO: replace Rect uses with integers.

    public void drawRect(Rect rect, Paint paint) {
        box.set(rect.left - dx, rect.top - dy, rect.right - dx, rect.bottom - dy);

        // skip if offscreen
        if (box.right < 0 || box.left > width) return;
        if (box.bottom < 0 || box.top > height) return;

        drawCount++;
        canvas.drawRect(box, paint);
    }

    public void drawCircle(float x, float y, float r, Paint paint) {
        int cx = (int)x - dx;
        int cy = (int)y - dy;

        // skip if offscreen
        if (cx < -r || cx > width+r) return;
        if (cy < -r || cy > height+r) return;

        drawCount++;
        canvas.drawCircle(x-dx, y-dy, r, paint);
    }

    public void drawBitmap(Bitmap img, Rect src, Rect dst, Paint paint) {
        box.set(dst.left - dx, dst.top - dy, dst.right - dx, dst.bottom - dy);

        // skip if offscreen
        if (box.right < 0 || box.left > width) return;
        if (box.top < 0 || box.bottom > height) return;

        drawCount++;
        canvas.drawBitmap(img, src, box, paint);
    }
}
