package com.ieb.smalltest;
import android.annotation.SuppressLint;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.ieb.smalltest.world.Level;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

@SuppressLint("ViewConstructor")
public class FirstScreen extends BaseView {
    private final Paint mPaint = new Paint();
    private final Main main;

    private boolean frameActive;
    private int physicsFrameCount, drawFrameCount;
    private long lastPhysicsTimeMs, lastDrawTimeMs;
    private double totalSeconds;

    private String lastEvent = "-";

    private final Level level;
    private int lastHeight, lastWidth, minDim; // dimensions of screen last time we did a paint.

    public FirstScreen(final Main context) throws IOException {
        super(context);
        this.main = context;
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

        long time = System.currentTimeMillis();

        if (physicsFrameCount > 1){
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
        minDim = Math.min(lastWidth, lastHeight);

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

        level.Draw(canvas, mPaint, lastWidth, lastHeight, (int)frameMs);

        Os.setSize(mPaint, 50);
        Os.boxText(canvas, lastEvent, 10.0f, 80.0f, mPaint);
        Os.boxText(canvas, "t=" + totalSeconds+"; d="+frameMs+";", 10.0f, lastHeight - 80.0f, mPaint);
    }

    private boolean rightIsUp = false, leftIsUp = false;

    @Override
    public boolean motionEvent(MotionEvent event) {
        // Turn touch events into button events
        /*

          | jump |action|  jump |
          |------+------+-------|
          | left | down | right |


          Pressing left + right means jump. If holding right, tapping left starts a jump.
         */

        StringBuilder sb = new StringBuilder();

        int pointerCount = event.getPointerCount();
        for (int i = 0; i < pointerCount; i++) {
            int type = event.getToolType(i);
            sb.append("ptr").append(i).append(": ");
            if (type == MotionEvent.TOOL_TYPE_FINGER) sb.append("touch ");
            if (type == MotionEvent.TOOL_TYPE_MOUSE) sb.append("mouse ");
            if (type == MotionEvent.TOOL_TYPE_STYLUS) sb.append("pen ");
            if (type == MotionEvent.TOOL_TYPE_UNKNOWN) sb.append("pad ");

            sb.append("x[").append(i).append("]=").append(event.getAxisValue(MotionEvent.AXIS_X, i)).append("; "); // left stick L/R
            sb.append("y[").append(i).append("]=").append(event.getAxisValue(MotionEvent.AXIS_Y, i)).append("; "); // left stick Up/Dn
            sb.append("z[").append(i).append("]=").append(event.getAxisValue(MotionEvent.AXIS_Z, i)).append("; "); // right stick L/R
            sb.append("dx[").append(i).append("]=").append(event.getAxisValue(MotionEvent.AXIS_HAT_X, i)).append("; "); // D-pad L/R
            sb.append("dy[").append(i).append("]=").append(event.getAxisValue(MotionEvent.AXIS_HAT_Y, i)).append("; "); // D-pad Up/Dn
            sb.append("L[").append(i).append("]=").append(event.getAxisValue(MotionEvent.AXIS_BRAKE, i)).append("; "); // Left trigger
            sb.append("R[").append(i).append("]=").append(event.getAxisValue(MotionEvent.AXIS_GAS, i)).append("; "); // Right trigger
            sb.append("Lt[").append(i).append("]=").append(event.getAxisValue(MotionEvent.AXIS_LTRIGGER, i)).append("; "); // ?
            sb.append("Rt[").append(i).append("]=").append(event.getAxisValue(MotionEvent.AXIS_RTRIGGER, i)).append("; "); // ?
            sb.append("rx[").append(i).append("]=").append(event.getAxisValue(MotionEvent.AXIS_RX, i)).append("; "); // ?
            sb.append("ry[").append(i).append("]=").append(event.getAxisValue(MotionEvent.AXIS_RY, i)).append("; "); // ?
            sb.append("rz[").append(i).append("]=").append(event.getAxisValue(MotionEvent.AXIS_RZ, i)).append("; "); // right stick Up/Dn
        }
        lastEvent = sb.toString();

        // virtual button states
        boolean up = false, left = false, right = false, down = false, action = false;

        // TODO: this logic is for touch. Move out to its own function.
        if (event.getAction() != MotionEvent.ACTION_UP) {// 'ACTION_UP' means all touch points up?
            // Scan all touch points
            for (int i = 0; i < pointerCount; i++) {
                //if (event.getPressure(i) < 0.5) continue;
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

        // Handle tap on opposite direction as jump
        if (!left && !right) {
            rightIsUp = false;
            leftIsUp = false;
        }

        if (left && (leftIsUp || level.isInputRightHeld())){
            left = false;
            up = true;
            leftIsUp = true;
        } else if (right && (rightIsUp || level.isInputLeftHeld())) {
            right = false;
            up = true;
            rightIsUp = true;
        }

        level.input_up(up);
        level.input_down(down);
        level.input_left(left);
        level.input_right(right);
        level.input_action(action);

        invalidate();
        return true;
    }

    // Scan codes for my gamepad:
    public static final int PAD_X = 307;
    public static final int PAD_Y = 308;
    public static final int PAD_A = 304;
    public static final int PAD_B = 305;
    public static final int PAD_L1 = 0;
    public static final int PAD_R1 = 0;
    public static final int PAD_START = 315;
    public static final int PAD_SELECT = 314; // Should be exit

    @Override
    public boolean keyEvent(KeyEvent event) {

        lastEvent = event.toString();

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

            case KeyEvent.KEYCODE_BACK: {
                main.Close();
                break;
            }
        }

        invalidate();
        return false;
    }
}

