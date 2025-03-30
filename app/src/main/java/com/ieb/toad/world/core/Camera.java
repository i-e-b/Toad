package com.ieb.toad.world.core;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.ieb.toad.sprite.core.Animation;

import org.jetbrains.annotations.NotNull;

/** Helper to draw on a canvas with an offset */
public class Camera {
    private Canvas canvas;
    private int dx,dy;
    private int cx,cy;
    private Rect dstRect, srcRect;
    private int width, height;
    private int drawCount;
    private final Paint paint = new Paint();

    /** @noinspection NullableProblems*/
    @Override
    public String toString() {
        return "("+cx+","+cy+")";
    }

    /** Set the canvas to draw into */
    public void use(@NotNull Canvas canvas) {
        this.canvas = canvas;
        width = canvas.getWidth();
        height = canvas.getHeight();
        dx = 0;
        dy = 0;
        dstRect = new Rect();
        srcRect = new Rect();
    }

    public void resetCount(){
        drawCount = 0;
    }

    public int getCount(){
        return drawCount;
    }

    /** Set the camera offset */
    public void centreOn(double x, double y) {
        cx = (int)x;
        cy = (int)y;
        dx = cx - (width / 2);
        dy = cy - (height / 2);
    }

    public void drawRect(Rect rect) {
        dstRect.set(rect.left - dx, rect.top - dy, rect.right - dx, rect.bottom - dy);

        // skip if offscreen
        if (dstRect.right < 0 || dstRect.left > width) return;
        if (dstRect.bottom < 0 || dstRect.top > height) return;

        drawCount++;
        canvas.drawRect(dstRect, paint);
    }

    public void drawCircle(float x, float y, float r) {
        int cx = (int)x - dx;
        int cy = (int)y - dy;

        // skip if offscreen
        if (cx < -r || cx > width+r) return;
        if (cy < -r || cy > height+r) return;

        drawCount++;
        canvas.drawCircle(x-dx, y-dy, r, paint);
    }

    public void drawBitmap(Bitmap img, int left, int top, int scale) {
        if (img == null) return;
        drawCount++;

        int w = img.getWidth();
        int h = img.getHeight();

        srcRect.set(0,0, w, h);
        dstRect.set(left-dx, top-dy, left-dx + w*scale, top-dy + h*scale);

        // skip if offscreen
        //if (dstRect.right < 0 || dstRect.left > width) return;
        //if (dstRect.top < 0 || dstRect.bottom > height) return;

        canvas.drawBitmap(img, srcRect, dstRect, null);
    }

    public void setARGB(int a, int r, int g, int b) {
        paint.setARGB(a,r,g,b);
    }

    /** Draw an animation sprite over a rectangle.
     * The sprite will be centred horizontally, and aligned to
     * the baseline of dst. The sprite may be larger or smaller than dest.*/
    public void drawSprite(Animation a, Rect dst) {
        Rect src = a.rect();

        int w = src.width() * a.scale;
        int hw = w / 2;
        int cx = dst.centerX();
        int left = cx - hw;
        int h = src.height() * a.scale;

        dstRect.set(left - dx, dst.bottom - h - dy, left - dx + w, dst.bottom - dy);

        // skip if offscreen
        if (dstRect.right < 0 || dstRect.left > width) return;
        if (dstRect.top < 0 || dstRect.bottom > height) return;

        drawCount++;
        canvas.drawBitmap(a.bitmap(), a.rect(), dstRect, null);
    }

    /** Draw an animation sprite over a circle.
     * The sprite will be centred horizontally, and aligned to
     * the baseline. The sprite may be larger or smaller than the circle.*/
    public void drawSprite(Animation a, double px, double py, double radius) {
        Rect src = a.rect();

        int w = src.width() * a.scale;
        int hw = w / 2;
        int left = (int)(px - hw);
        int h = src.height() * a.scale;
        int b = (int)(py+radius);

        dstRect.set(left - dx, b - h - dy, left - dx + w, b - dy);

        // skip if offscreen
        if (dstRect.right < 0 || dstRect.left > width) return;
        if (dstRect.top < 0 || dstRect.bottom > height) return;

        drawCount++;
        canvas.drawBitmap(a.bitmap(), src, dstRect, null);
    }

    /** Get the rectangle being displayed by the camera */
    public Rect getCoverage() {
        return null;
    }
}
