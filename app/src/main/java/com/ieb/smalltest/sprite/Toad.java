package com.ieb.smalltest.sprite;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import com.ieb.smalltest.Main;
import com.ieb.smalltest.input.VirtualGamepad;
import com.ieb.smalltest.world.Camera;
import com.ieb.smalltest.world.Collision;
import com.ieb.smalltest.world.Level;
import com.ieb.smalltest.world.Thing;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;

public class Toad extends Thing {

    private final Animation run_left = new Animation(64, Animation.FOREVER, 16, 28, 29, new int[]{1, 18, 35, 18});
    private final Animation run_right = new Animation(64, Animation.FOREVER, 16, 28, 62, new int[]{205,188,171,188});
    private final Animation carry_left = new Animation(100, Animation.FOREVER, 16, 28, 29, new int[]{52,69,86,69});
    private final Animation carry_right = new Animation(100, Animation.FOREVER, 16, 28, 62, new int[]{154,134,120,134});

    private final Animation enter_left = new Animation(500, Animation.ONCE, 16, 28, 29, new int[]{103});
    private final Animation enter_right = new Animation(500, Animation.ONCE, 16, 28, 62, new int[]{103});

    private final Animation fall_left = new Animation(100, Animation.FOREVER, 16, 28, 29, new int[]{120});
    private final Animation fall_right = new Animation(100, Animation.FOREVER, 16, 28, 62, new int[]{86});

    private final Animation pull_left = new Animation(50, Animation.ONCE, 16, 28, 29, new int[]{171,188});
    private final Animation pull_right = new Animation(50, Animation.ONCE, 16, 28, 62, new int[]{35,18});

    private final Animation hurt = new Animation(500, Animation.ONCE, 16, 28, 29, new int[]{205});

    private final Animation duck = new Animation(100, Animation.FOREVER, 16, 28, 29, new int[]{137});
    private final Animation duck_carry = new Animation(100, Animation.FOREVER, 16, 28, 29, new int[]{154});


    private long jumpTimeLeftMs;
    private double lastFramePx;
    private final Bitmap mToad;

    /** Load Toad graphics */
    public Toad(final Main context) throws IOException {
        AssetManager assets = context.getAssets();
        hitBox = new Rect(0,0,0,0);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        InputStream toadFile = assets.open("toad.png");
        mToad = BitmapFactory.decodeStream(toadFile, hitBox, options);
        toadFile.close();
        type = Collision.PLAYER;
        radius = 32;
        gravity = 1.0; // fully affected by gravity
    }


    /** User control is done during AI think time */
    @Override
    public void think(Level level, int ms) {
        // apply control
        mapControls();
        applyControlsToPhysics(ms);

        // copy 'prev' button states for next frame
        postFrameControlUpdate();
    }


    private void postFrameControlUpdate() {
        prevBtnAction= btnAction;
        prevBtnUp = btnUp;
        prevBtnDown = btnDown;
        prevBtnRight = btnRight;
        prevBtnLeft = btnLeft;
    }

    private void applyControlsToPhysics(int ms) {
        if (btnRight) addPlayerSpeed(50, 0);
        if (btnLeft) addPlayerSpeed(-50, 0);

        if (btnUp){
            if (jumpTimeLeftMs > 0){
                jumpTimeLeftMs -= ms;
                v0y = -900;
            }
        } else {
            jumpTimeLeftMs = 300;
        }
    }

    /** Map current VirtualGamepad state to level controls */
    private void mapControls() {
        btnDown = VirtualGamepad.isDown();
        btnRight = VirtualGamepad.isRight();
        btnUp = VirtualGamepad.isUp();
        btnLeft = VirtualGamepad.isLeft();
        btnAction = VirtualGamepad.isAction();
    }

    public void addPlayerSpeed(double dx, int dy) {
        v0y += dy;
        v0x += dx;
    }

    private boolean btnAction, btnUp, btnDown, btnRight, btnLeft;
    private boolean prevBtnAction, prevBtnUp, prevBtnDown, prevBtnRight, prevBtnLeft;

    @Override
    public void draw(@NotNull Camera camera) {
        double dx = Math.abs(p0x - lastFramePx);
        lastFramePx = p0x;

        Animation a = run_left;
        if (v0x > 0) a = run_right;

        a.advance((int) dx); // animate based on movement

        hitBox.bottom = (int) ((int) p1y + radius);
        hitBox.top = hitBox.bottom - (28 * 4);
        hitBox.left = (int) p1x - (int) radius;
        hitBox.right = (int) p1x + (int) radius;

        camera.drawBitmap(mToad, a.rect(), hitBox);
    }
}
