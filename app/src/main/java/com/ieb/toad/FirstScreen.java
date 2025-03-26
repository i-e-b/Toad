package com.ieb.toad;
import android.annotation.SuppressLint;
import com.ieb.toad.input.*;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.ieb.toad.world.core.Camera;
import com.ieb.toad.world.Level;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@SuppressLint("ViewConstructor")
public class FirstScreen extends BaseView {
    private final Paint mPaint = new Paint();
    private final Main main;
    private final Camera camera;

    private boolean frameActive;
    private int physicsFrameCount, drawFrameCount;
    private long lastPhysicsTimeMs, lastDrawTimeMs;
    private double totalSeconds;

    private final Level level;
    private int lastHeight, lastWidth; // dimensions of screen last time we did a paint.

    public FirstScreen(final Main context) throws IOException {
        super(context);
        this.main = context;
        frameActive = false;
        camera = new Camera();

        level = new Level(context);

        mPaint.setAntiAlias(true);
        mPaint.setFilterBitmap(false);
        mPaint.setDither(false);
        mPaint.setBlendMode(BlendMode.SRC_OVER);
        this.setBackgroundColor(0xFF000000);
    }

    /** Action on timer. Does physics and triggered frame draw
     * @param time system time in milliseconds
     */
    @Override
    protected void OnTimerTick(long time){
        if (frameActive) return;

        // Do frame logic, call invalidate
        frameActive = true;
        if (lastPhysicsTimeMs > time) lastPhysicsTimeMs = time; // clock wrapped. Shouldn't really happen.

        if (physicsFrameCount > 1){
            // Do simulation
            lastPhysicsTimeMs += level.stepMillis(time - lastPhysicsTimeMs);
        } else {
            lastPhysicsTimeMs = time;
        }
        physicsFrameCount++;

        frameActive = false;
        invalidate();
    }

    @Override
    public void onDraw(@NotNull final Canvas canvas) {
        lastWidth = getWidth();
        lastHeight = getHeight();
        VirtualGamepad.setTouchSize(lastWidth, lastHeight);

        if (frameActive) {
            mPaint.setARGB(255, 255, 0, 0);
        } else {
            mPaint.setARGB(255, 128, 0, 255);
        }

        long frameMs = 1;
        if (physicsFrameCount > 0) {
            if (drawFrameCount > 0) {
                frameMs = lastPhysicsTimeMs - lastDrawTimeMs;
            }
            lastDrawTimeMs = lastPhysicsTimeMs;
            drawFrameCount++;
        }

        totalSeconds += frameMs / 1000.0;

        camera.resetCount();
        camera.use(canvas);
        level.Draw(camera, lastWidth, lastHeight, (int)frameMs);

        VirtualGamepad.draw(canvas, mPaint, lastWidth);

        int dc = camera.getCount();
        mPaint.setARGB(120,0,255,255);
        Os.setSize(mPaint, 50);
        Os.boxText(canvas, "t=" + ((int)totalSeconds)+"; ft="+frameMs+"; dc="+dc+"; it="+idleTime+";", 10.0f, lastHeight - 80.0f, mPaint);
    }
}

