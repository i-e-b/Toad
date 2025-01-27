package com.ieb.smalltest;
import android.annotation.SuppressLint;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.ieb.smalltest.world.Level;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;

@SuppressLint("ViewConstructor")
public class FirstScreen extends BaseView {
    private final Paint mPaint = new Paint();
    private final Main parent;

    private boolean frameActive;
    private int frameCount;
    private int lastRpt, lastAct;
    private long lastTime;

    private final boolean darkColors; // night mode if true.

    private Level level;
    private Rect src, dst, dummy;
    private Bitmap mTiles;
    private final AssetManager assets;
    private int lastHeight, lastWidth; // dimensions of screen last time we did a paint.

    public FirstScreen(final Main context) throws IOException {
        super(context);
        parent = context;
        frameActive = false;

        assets = context.getAssets();
        dummy = new Rect(0, 0, 0, 0);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        level = new Level(context);

        InputStream tileFile = assets.open("tile.png");
        mTiles = BitmapFactory.decodeStream(tileFile, dummy, options);
        tileFile.close();

        src = new Rect();
        dst = new Rect();

        // Check for dark mode.
        darkColors = Os.isDarkMode(this);

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
        int action = event.getAction();
        Log.i("FS", "Action: "+action);
        switch (action){
            case MotionEvent.ACTION_UP:{
                Log.i("FS", "Action up");
                break;
            }
            case MotionEvent.ACTION_DOWN:{
                Log.i("FS", "Action down");
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                Log.i("FS", "Action move");
                break;
            }
        }

        invalidate();
        return true;
    }

    @Override
    public boolean keyEvent(KeyEvent event) {
        lastAct = event.getAction();
        lastRpt = Math.max(lastRpt, event.getRepeatCount());
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

