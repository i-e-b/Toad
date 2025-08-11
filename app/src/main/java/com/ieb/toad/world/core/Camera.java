package com.ieb.toad.world.core;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import com.ieb.toad.FirstScreen;
import com.ieb.toad.sprite.core.Animation;
import com.ieb.toad.world.loader.CameraZone;

import org.jetbrains.annotations.NotNull;

import java.util.List;

/** Helper to draw on a canvas with an offset */
public class Camera {
    private final FirstScreen screen;
    private Canvas canvas;
    private int left, top;
    private int cx,cy;
    private Rect dstRect, srcRect;
    private int width, height;
    private int drawCount;
    private final Paint paint = new Paint();
    private CameraZone lastZone;

    public Camera(FirstScreen screen) {
        this.screen = screen;
    }

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
        left = 0;
        top = 0;
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
    public void centreOn(double x, double y, List<CameraZone> camZones) {
        lastZone = null;

        cx = (int)x;
        cy = (int)y;

        for (int i = 0; i < camZones.size(); i++) {
            CameraZone cz = camZones.get(i);
            if (cz.rect.contains(cx,cy)) {
                lastZone = cz;
                break;
            }
        }

        left = cx - (width / 2);
        top = cy - (height / 2);

        if (lastZone == null) return; // No zone. just centre the cam

        //screen.setBackgroundColor(lastZone.color);

        // Force the view inside the zone
        if (top < lastZone.rect.top){
            top = lastZone.rect.top;
        }
        int bottom = top + height;
        if (bottom > lastZone.rect.bottom){
            int dy = bottom - lastZone.rect.bottom;
            top -= dy;
        }

        int right = left + width;
        if (right > lastZone.rect.right){
            int dx = right - lastZone.rect.right;
            left -= dx;
        }
        if (left < lastZone.rect.left){
            left = lastZone.rect.left;
        }
    }

    public void drawRect(Rect rect) {
        dstRect.set(rect.left - left, rect.top - top, rect.right - left, rect.bottom - top);

        // skip if offscreen
        if (dstRect.right < 0 || dstRect.left > width) return;
        if (dstRect.bottom < 0 || dstRect.top > height) return;

        drawCount++;
        canvas.drawRect(dstRect, paint);
    }

    public void drawBitmap(Bitmap img, int left, int top, int scale) {
        if (img == null) return;
        drawCount++;

        int w = img.getWidth();
        int h = img.getHeight();

        srcRect.set(0,0, w, h);
        dstRect.set(left- this.left, top- this.top, left- this.left + w*scale, top- this.top + h*scale);

        // skip if offscreen
        if (dstRect.right < 0 || dstRect.left > width) return;
        if (dstRect.bottom < 0 || dstRect.top > height) return;

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

        dstRect.set(left - this.left, dst.bottom - h - top, left - this.left + w, dst.bottom - top);

        // skip if offscreen
        if (dstRect.right < 0 || dstRect.left > width) return;
        if (dstRect.bottom < 0 || dstRect.top > height) return;

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

        dstRect.set(left - this.left, b - h - top, left - this.left + w, b - top);

        // skip if offscreen
        if (dstRect.right < 0 || dstRect.left > width) return;
        if (dstRect.bottom < 0 || dstRect.top > height) return;

        drawCount++;
        canvas.drawBitmap(a.bitmap(), src, dstRect, null);
    }

    /** Get the rectangle being displayed by the camera */
    public Rect getCoverage() {
        return null;
    }

    /** Clear canvas to current cam zone color, or the default if none set. */
    public void clear(int defaultColor) {
        int color = defaultColor;
        if (lastZone != null && lastZone.color != 0) color = lastZone.color;

        canvas.drawColor(0xFF000000 | color);
    }
}
