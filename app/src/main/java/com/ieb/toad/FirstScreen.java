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
    private final Camera camera;

    private boolean frameActive;
    private int physicsFrameCount, drawFrameCount;
    private long lastPhysicsTimeMs, lastDrawTimeMs;
    private double totalSeconds;

    private final Level level;

    public FirstScreen(final Main context) throws IOException {
        super(context);
        frameActive = false;
        camera = new Camera();

        // TODO: move this out of constructor, show loading screen
        level = new Level(context);

        mPaint.setAntiAlias(true);
        mPaint.setFilterBitmap(false);
        mPaint.setDither(false);
        mPaint.setBlendMode(BlendMode.SRC_OVER);
        this.setBackgroundColor(level.getBackgroundColor());
    }

    /** Action on timer. Does physics and triggered frame draw
     * @param time system time in milliseconds
     */
    @Override
    protected void OnSimulationTimerTick(long time){
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

    /** Override to perform background actions */
    protected void OnBackgroundTimerTick(){
        if (!level.loadedOk) return;
        level.backgroundUpdates(camera);
    }

    @Override
    public void onDraw(@NotNull final Canvas canvas) {
        // dimensions of screen last time we did a paint.
        int width = getWidth();
        int height = getHeight();

        // check loaded level
        if (!level.loadedOk){
            mPaint.setARGB(255,200,0,0);
            Os.setSize(mPaint, 50);
            Os.boxText(canvas, "Level not loaded!", 10.0f, 80.0f, mPaint);
            return;
        }

        VirtualGamepad.setTouchSize(width, height);

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
        level.Draw(camera, width, height, (int)frameMs);

        VirtualGamepad.draw(canvas, mPaint, width);

        int dc = camera.getCount();
        mPaint.setARGB(120,0,0,0);
        Os.setSize(mPaint, 50);
        Os.boxText(canvas, "t=" + ((int)totalSeconds)+"; ft="+frameMs+"; dc="+dc+"; it="+idleTime,
                10.0f, height - 80.0f, mPaint);
    }
}

