package com.ieb.toad;
import android.annotation.SuppressLint;
import com.ieb.toad.input.*;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.Paint;

import com.ieb.toad.world.core.Camera;
import com.ieb.toad.world.Simulation;
import com.ieb.toad.world.loader.TiledLoader;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@SuppressLint("ViewConstructor")
public class LevelScreen extends BaseView {
    private final Paint mPaint = new Paint();
    private final Camera camera;

    private boolean frameActive;
    private int physicsFrameCount, drawFrameCount;
    private long lastPhysicsTimeMs, lastDrawTimeMs;
    private double totalSeconds;

    private final Simulation simulation;

    public LevelScreen(final Main context, int levelToLoad) throws IOException {
        super(context);
        frameActive = false;
        camera = new Camera(this);

        // TODO: move this out of constructor, show loading screen
        // TODO: show loading message, do this out of constructor

        var level = new TiledLoader(context);
        var loadedOk = level.loadLevel(levelToLoad);
        simulation = new Simulation(level);

        mPaint.setAntiAlias(true);
        mPaint.setFilterBitmap(false);
        mPaint.setDither(false);
        mPaint.setBlendMode(BlendMode.SRC_OVER);
        this.setBackgroundColor(simulation.getBackgroundColor());
    }

    /** Action on timer. Does physics and triggered frame draw
     * @param time system time in milliseconds
     */
    @Override
    protected synchronized void OnSimulationTimerTick(long time){
        if (frameActive) return;

        // Do frame logic, call invalidate
        frameActive = true;
        if (lastPhysicsTimeMs > time) lastPhysicsTimeMs = time; // clock wrapped. Shouldn't really happen.

        if (physicsFrameCount > 1) {
            // Do simulation
            lastPhysicsTimeMs += simulation.stepMillis(time - lastPhysicsTimeMs);
        } else {
            lastPhysicsTimeMs = time;
        }
        physicsFrameCount++;

        frameActive = false;
        invalidate();
    }

    /** Override to perform background actions */
    protected synchronized void OnBackgroundTimerTick(){
        simulation.backgroundUpdates(camera);
    }

    @Override
    public void onDraw(@NotNull final Canvas canvas) {
        // dimensions of screen last time we did a paint.
        int width = getWidth();
        int height = getHeight();

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
        simulation.Draw(camera, (int)frameMs);

        VirtualGamepad.draw(canvas, mPaint, width);

        int dc = camera.getCount();
        mPaint.setARGB(120,0,0,0);
        Os.setSize(mPaint, 50);
        Os.boxText(canvas, "t=" + ((int)totalSeconds)+"; ft="+frameMs+"; dc="+dc+"; it="+idleTime,
                10.0f, height - 80.0f, mPaint);
    }
}

