package com.ieb.toad.input;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

import org.jetbrains.annotations.NotNull;

/** Basic static model of a single gamepad for input.
 * This reads keyboard, touchscreen, and actual gamepad for input. */
public class VirtualGamepad {
    private static final String TAG = "VPad";

    /** True if we are getting a 'left' signal somewhere */
    public static boolean isLeft(){
        return DPadHorz < -0.5 || LeftStickHorz < -0.5;
    }
    /** True if we are getting a 'right' signal somewhere */
    public static boolean isRight(){
        return DPadHorz > 0.5 || LeftStickHorz > 0.5;
    }
    /** True if we are getting a 'up' signal somewhere */
    public static boolean isUp(){
        return DPadVert < -0.5 || LeftStickVert < -0.5 || ButtonA > 0.5;
    }
    /** True if we are getting a 'down' signal somewhere */
    public static boolean isDown(){
        return DPadVert > 0.5 || LeftStickVert > 0.5;
    }
    /** True if we are getting a 'action' signal somewhere */
    public static boolean isAction(){
        return ButtonX > 0.5;
    }
    /** True if we are pressing the 'select' button */
    public static boolean isSelect(){
        return ButtonSelect > 0.5;
    }

    /** D-pad X axis. -1.0 is left, +1.0 is right */
    public static float DPadHorz = 0.0f;
    /** D-pad Y axis. -1.0 is up, +1.0 is down */
    public static float DPadVert = 0.0f;

    /** Left analog stick X axis. -1.0 is full left, +1.0 is full right */
    public static float LeftStickHorz = 0.0f;
    /** Left analog stick Y axis. -1.0 is full up, +1.0 is full down */
    public static float LeftStickVert = 0.0f;
    /** Left stick press. 0.0 is up, +1.0 is down */
    public static float ButtonL3 = 0.0f;

    /** Right analog stick X axis. -1.0 is full left, +1.0 is full right */
    public static float RightStickHorz = 0.0f;
    /** Right analog stick Y axis. -1.0 is full up, +1.0 is full down */
    public static float RightStickVert = 0.0f;
    /** Right stick press. 0.0 is up, +1.0 is down */
    public static float ButtonR3 = 0.0f;


    /** Left analog trigger. 0.0 is up, +1.0 is down */
    public static float LTrigger = 0.0f;
    /** Right analog trigger. 0.0 is up, +1.0 is down */
    public static float RTrigger = 0.0f;

    /** Left digital trigger. 0.0 is up, +1.0 is down */
    public static float ButtonL1 = 0.0f;
    /** Right digital trigger. 0.0 is up, +1.0 is down */
    public static float ButtonR1 = 0.0f;

    /** A button (PS cross) 0.0 is released, 1.0 is pushed*/
    public static float ButtonA = 0.0f;
    /** B button (PS circle) 0.0 is released, 1.0 is pushed*/
    public static float ButtonB = 0.0f;
    /** X button (PS square) 0.0 is released, 1.0 is pushed*/
    public static float ButtonX = 0.0f;
    /** Y button (PS triangle) 0.0 is released, 1.0 is pushed*/
    public static float ButtonY = 0.0f;

    /** Start button (PS Options) 0.0 is released, 1.0 is pushed*/
    public static float ButtonStart = 0.0f;
    /** Select button (PS Share) 0.0 is released, 1.0 is pushed*/
    public static float ButtonSelect = 0.0f;

    /** Update size of touch area. This is used for mapping touch to virtual buttons */
    public static void setTouchSize(int width, int height) {
        touchWidth = width;
        touchHeight = height;
    }

    /** Read an input event into the virtual controller model.
     * This amalgamates all 'pointers' into one controller, along with key inputs */
    public static void motionEvent(MotionEvent event) {
        int pointerCount = event.getPointerCount();
        boolean needTouchUpdate = false;
        for (int i = 0; i < pointerCount; i++) {
            int type = event.getToolType(i);

            switch (type){
                case MotionEvent.TOOL_TYPE_FINGER:
                case MotionEvent.TOOL_TYPE_MOUSE:
                case MotionEvent.TOOL_TYPE_STYLUS:
                    needTouchUpdate = true;
                    updateTouchInfo(event);
                    break;

                case MotionEvent.TOOL_TYPE_UNKNOWN:
                    handleGamepad(event, i);
                    break;

                case MotionEvent.TOOL_TYPE_ERASER: // nothing
                    break;
            }
        }

        // reset touches for pointers that are gone
        for (int i = pointerCount; i < 6; i++){
            touchDown[i] = false;
        }

        if (needTouchUpdate) mapTouchToPad();
    }

    /** Read an input event into the virtual controller model.
     * This amalgamates all key sources into one controller, along with other inputs */
    public static void keyEvent(KeyEvent event) {
        if (event.getRepeatCount() > 0) return;

        boolean isDown = event.getAction() == KeyEvent.ACTION_DOWN;

        int scanCode = event.getScanCode();
        switch (scanCode) {
            case PAD_X: ButtonX = isDown ? 1.0f : 0.0f;break;
            case PAD_Y: ButtonY = isDown ? 1.0f : 0.0f;break;
            case PAD_A: ButtonA = isDown ? 1.0f : 0.0f;break;
            case PAD_B: ButtonB = isDown ? 1.0f : 0.0f;break;
            case PAD_L1: ButtonL1 = isDown ? 1.0f : 0.0f;break;
            case PAD_R1: ButtonR1 = isDown ? 1.0f : 0.0f;break;
            case PAD_L3: ButtonL3 = isDown ? 1.0f : 0.0f;break;
            case PAD_R3: ButtonR3 = isDown ? 1.0f : 0.0f;break;
            case PAD_START: ButtonStart = isDown ? 1.0f : 0.0f;break;
            case PAD_SELECT: ButtonSelect = isDown ? 1.0f : 0.0f;break;
        }

        int keyCode = event.getKeyCode();
        switch (keyCode){
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_SPACE: {
                if (scanCode != PAD_A && scanCode != PAD_B && scanCode != PAD_X && scanCode != PAD_Y
                && scanCode != PAD_R3 && scanCode != PAD_L3) {
                    ButtonX = isDown ? 1.0f : 0.0f;
                }
                break;
            }

            case KeyEvent.KEYCODE_DPAD_RIGHT: DPadHorz = isDown ? 1.0f : 0.0f; break;
            case KeyEvent.KEYCODE_DPAD_LEFT: DPadHorz = isDown ? -1.0f : 0.0f; break;
            case KeyEvent.KEYCODE_DPAD_DOWN: DPadVert = isDown ? 1.0f : 0.0f; break;
            case KeyEvent.KEYCODE_DPAD_UP: DPadVert = isDown ? -1.0f : 0.0f; break;
        }
    }

    /** rect to help draw() */
    private static final Rect box = new Rect(0,0,0,0);

    /** Draw a diagram of the virtual controller in the top left of the canvas */
    public static void draw(@NotNull Canvas canvas, Paint paint, int w, int h) {
        // Triggers
        paint.setARGB((int)(200 * RTrigger)+55, 255, 255, 255);
        box.set(w - 70, 20, w - 40, 40);
        canvas.drawRect(box, paint);

        paint.setARGB((int)(200 * ButtonR1)+55, 255, 255, 255);
        box.set(w - 70, 50, w - 40, 60);
        canvas.drawRect(box, paint);

        paint.setARGB((int)(200 * LTrigger)+55, 255, 255, 255);
        box.set(w - 230, 20, w - 200, 40);
        canvas.drawRect(box, paint);

        paint.setARGB((int)(200 * ButtonL1)+55, 255, 255, 255);
        box.set(w - 230, 50, w - 200, 60);
        canvas.drawRect(box, paint);

        // Main face buttons
        paint.setARGB((int)(200 * ButtonX)+55, 255, 0, 255);
        box.set(w - 80, 110, w - 70, 120);
        canvas.drawRect(box, paint);

        paint.setARGB((int)(200 * ButtonY)+55, 128, 255, 128);
        box.set(w - 70, 80, w - 60, 90);
        canvas.drawRect(box, paint);

        paint.setARGB((int)(200 * ButtonA)+55, 128, 128, 255);
        box.set(w - 50, 130, w - 40, 140);
        canvas.drawRect(box, paint);

        paint.setARGB((int)(200 * ButtonB)+55, 255, 128, 128);
        box.set(w - 40, 100, w - 30, 110);
        canvas.drawRect(box, paint);

        // DPad
        paint.setARGB(100, 255, 255, 255);
        box.set(w - 210, 100, w - 190, 120);
        canvas.drawRect(box, paint);
        int dx = 205 - (int)(DPadHorz*10);
        int dy = 105 + (int)(DPadVert*10);
        paint.setARGB(150, 255, 255, 255);
        box.set(w - dx, dy, w - dx + 10, dy + 10);
        canvas.drawRect(box, paint);

        // Start/Select
        paint.setARGB((int)(200 * ButtonStart)+55, 255, 255, 255);
        box.set(w - 110, 60, w - 100, 70);
        canvas.drawRect(box, paint);

        paint.setARGB((int)(200 * ButtonSelect)+55, 255, 255, 255);
        box.set(w - 170, 60, w - 160, 70);
        canvas.drawRect(box, paint);

        // L stick
        if (ButtonL3 > 0.5) paint.setARGB(255, 128, 255, 128);
        else  paint.setARGB(100, 255, 255, 255);
        box.set(w - 180, 150, w - 160, 170);
        canvas.drawRect(box, paint);
        dx = 175 - (int)(LeftStickHorz*10);
        dy = 155 + (int)(LeftStickVert*10);
        paint.setARGB(150, 255, 255, 255);
        box.set(w - dx, dy, w - dx + 10, dy + 10);
        canvas.drawRect(box, paint);

        // R stick
        if (ButtonR3 > 0.5) paint.setARGB(255, 128, 255, 128);
        else  paint.setARGB(100, 255, 255, 255);
        box.set(w - 100, 150, w - 80, 170);
        canvas.drawRect(box, paint);
        dx = 95 - (int)(RightStickHorz*10);
        dy = 155 + (int)(RightStickVert*10);
        paint.setARGB(150, 255, 255, 255);
        box.set(w - dx, dy, w - dx + 10, dy + 10);
        canvas.drawRect(box, paint);
    }


    private static int touchWidth;
    private static int touchHeight;

    // Scan codes for my gamepad: (Generic "XInput")
    private static final int PAD_X = 307;
    private static final int PAD_Y = 308;
    private static final int PAD_A = 304;
    private static final int PAD_B = 305; // defaults to exit, needs an override
    private static final int PAD_L1 = 310;
    private static final int PAD_R1 = 311;
    private static final int PAD_START = 315;
    private static final int PAD_SELECT = 314; // Should be exit
    private static final int PAD_L3 = 317; // Push down on left stick
    private static final int PAD_R3 = 318; // Push down on right stick

    /** Map gamepad inputs  */
    private static void handleGamepad(MotionEvent event, int pointer) {
        LeftStickHorz = event.getAxisValue(MotionEvent.AXIS_X, pointer);
        LeftStickVert = event.getAxisValue(MotionEvent.AXIS_Y, pointer);

        RightStickHorz = event.getAxisValue(MotionEvent.AXIS_Z, pointer);
        RightStickVert = event.getAxisValue(MotionEvent.AXIS_RZ, pointer);

        LTrigger = event.getAxisValue(MotionEvent.AXIS_BRAKE, pointer);
        RTrigger = event.getAxisValue(MotionEvent.AXIS_GAS, pointer);

        DPadHorz = event.getAxisValue(MotionEvent.AXIS_HAT_X, pointer);
        DPadVert = event.getAxisValue(MotionEvent.AXIS_HAT_Y, pointer);
    }


    private static boolean rightIsUp = false, leftIsUp = false;

    // Touch input registers
    private static final float[] touchX = new float[6]; //!< up to 6 touch points
    private static final float[] touchY = new float[6];
    private static final boolean[] touchDown = new boolean[6];

    private static void updateTouchInfo(MotionEvent event){
        int act = event.getAction() & 0xFF;
        int idx = event.getActionIndex();
        if (idx >= 6) return;

        switch (act){
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN: // android sends these in duplicate
            case MotionEvent.ACTION_MOVE:
                touchX[idx] = event.getX(idx);
                touchY[idx] = event.getY(idx);
                touchDown[idx] = true;
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP: // android sends these in duplicate
                touchX[idx] = -1.0f;
                touchY[idx] = -1.0f;
                touchDown[idx] = false;
                break;

            default:
                touchDown[idx] = false;
                Log.i(TAG, "?"+act);
                break;
        }
    }

    /** Map touch to input. This is a bit complex and should be updated based on needs */
    private static void mapTouchToPad() {
        if (touchHeight < 3 || touchWidth < 3) return; // screen is not active yet
        /*
            Screen is split in 3 by width, then by top / bottom:

          | jump | action | jump  |
          |------+--------+-------|
          | left | down   | right |

          Pressing left + right means jump. If holding right, tapping left starts a jump.
          Action maps to ButtonX

          First we work out an internal logical state, then we map it back on to the virtual controls.
         */

        // virtual button states
        boolean up = false, left = false, right = false, down = false, action = false;

        // Scan all touch points
        for (int i = 0; i < touchDown.length; i++) {
            if (!touchDown[i]) continue;
            double fx = touchX[i] / (touchWidth + 1);
            double fy = touchY[i] / (touchHeight + 1);
            //event.getPointerCoords(i, ptr);
            //ptr.pressure

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

        // Handle tap on opposite direction as jump
        if (!left && !right) {
            rightIsUp = false;
            leftIsUp = false;
        }

        if (left && (leftIsUp || isRight())){
            left = false;
            up = true;
            leftIsUp = true;
        } else if (right && (rightIsUp || isLeft())) {
            right = false;
            up = true;
            rightIsUp = true;
        }

        // Reset first
        DPadVert = 0.0f;
        DPadHorz = 0.0f;
        ButtonX = 0.0f;

        // Map state onto virtual controls
        if (up) DPadVert = -1.0f;
        if (down) DPadVert = 1.0f;
        if (left) DPadHorz = -1.0f;
        if (right) DPadHorz = 1.0f;
        if (action) ButtonX = 1.0f;
    }
}
