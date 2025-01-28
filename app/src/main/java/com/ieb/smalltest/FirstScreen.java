package com.ieb.smalltest;
import android.annotation.SuppressLint;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.ieb.smalltest.world.Level;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@SuppressLint("ViewConstructor")
public class FirstScreen extends BaseView {
    private final Paint mPaint = new Paint();

    private boolean frameActive;
    private int frameCount;
    private long lastTime;

    private final Level level;
    private int lastHeight, lastWidth, minDim; // dimensions of screen last time we did a paint.

    public FirstScreen(final Main context) throws IOException {
        super(context);
        frameActive = false;

        level = new Level(context);

        mPaint.setAntiAlias(true);
        mPaint.setFilterBitmap(false);
        mPaint.setDither(false);
        mPaint.setBlendMode(BlendMode.SRC_OVER);
        this.setBackgroundColor(0xFF000000);
    }

    @Override
    protected void OnTimerTick(){
        if (frameActive) return;

        // Do frame logic, call invalidate
        frameActive = true;
        frameCount++;

        long time = System.currentTimeMillis();

        if (frameCount > 1){
            lastTime += level.stepMillis(time - lastTime);
        } else {
            lastTime = time;
        }

        frameActive = false;
        invalidate();
    }

    @Override
    public void onDraw(@NotNull final Canvas canvas) {
        lastWidth = getWidth();
        lastHeight = getHeight();
        minDim = Math.min(lastWidth, lastHeight);

        if (frameActive) {
            mPaint.setARGB(255, 255, 0, 0);
        } else {
            mPaint.setARGB(255, 128, 0, 255);
        }

        Os.setSize(mPaint, 50);
        Os.drawText(canvas, "f="+frameCount, 10.0f, 80.0f, mPaint);

        level.Draw(canvas, mPaint, lastWidth, lastHeight);
    }

    @Override
    public boolean motionEvent(MotionEvent event) {
        // Turn touch events into button events
        /*

          | jump |action|  jump |
          |------+------+-------|
          | left | down | right |


            Maybe: pressing left + right means jump? If holding right, tapping left starts a jump.
         */

        // virtual button states
        boolean up = false, left = false, right = false, down = false, action = false;

        if (event.getAction() != MotionEvent.ACTION_UP) {// 'ACTION_UP' means all touch points up?
            // Scan all touch points
            int count = event.getPointerCount();
            for (int i = 0; i < count; i++) {
                if (event.getPressure(i) < 0.5) continue;
                double fx = event.getX(i) / (lastWidth + 1);
                double fy = event.getY(i) / (lastHeight + 1);

                // TODO: finish this and test with multi-touch
                if (fx <= 0.33) { // left side
                    if (fy < 0.5) { // top-left
                        up = true;
                    } else {
                        left = true;
                    }
                } else if (fx >= 0.66) { // right side
                    if (fy < 0.5) { // top-right
                        up = true;
                    } else {
                        right = true;
                    }
                } else { // centre
                    if (fy < 0.5) {
                        action = true;
                    } else {
                        down = true;
                    }
                }
            }
        }

        level.input_up(up);
        level.input_down(down);
        level.input_left(left);
        level.input_right(right);
        level.input_action(action);

        invalidate();
        return true;
    }

    @Override
    public boolean keyEvent(KeyEvent event) {
        if (event.getRepeatCount() > 0) return true;

        boolean isDown = event.getAction() == KeyEvent.ACTION_DOWN;

        int keyCode = event.getKeyCode();
        //Log.i("FS", "Key: "+keyCode);
        switch (keyCode){
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_SPACE: {
                level.input_action(isDown);
                break;
            }

            case KeyEvent.KEYCODE_DPAD_RIGHT:{
                level.input_right(isDown);
                //level.playerSpeed(50, 0);
                break;
            }

            case KeyEvent.KEYCODE_DPAD_LEFT:{
                level.input_left(isDown);
                //level.playerSpeed(-50, 0);
                break;
            }

            case KeyEvent.KEYCODE_DPAD_DOWN:{
                level.input_down(isDown);
                break;
            }

            case KeyEvent.KEYCODE_DPAD_UP:{
                level.input_up(isDown);
                //level.playerSpeed(0, -70);
                break;
            }
        }

        invalidate();
        return false;
    }
}

