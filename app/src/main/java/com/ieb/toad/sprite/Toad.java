package com.ieb.toad.sprite;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import com.ieb.toad.input.VirtualGamepad;
import com.ieb.toad.world.core.Camera;
import com.ieb.toad.world.core.Collision;
import com.ieb.toad.world.Level;
import com.ieb.toad.world.core.Thing;

import org.jetbrains.annotations.NotNull;

public class Toad extends Thing {

    private final Animation run_left;
    private final Animation run_right;

    private int desireDirection = 1; // negative = left, positive = right.
    private long jumpTimeLeftMs;
    private double lastFramePx;
    private final SpriteSheet spriteSheet;

    /** Load Toad graphics */
    public Toad(final SpriteSheet spriteSheet) {
        this.spriteSheet = spriteSheet;

        run_left = new Animation(64, Animation.FOREVER, spriteSheet.toadTiles, new int[]{9,10,11,10});
        run_right = new Animation(64, Animation.FOREVER, spriteSheet.toadTiles, new int[]{36,35,34,35});

        hitBox = new Rect(0,0,0,0);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

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
    }

    private void applyControlsToPhysics(int ms) {
        if (btnRight) {
            addPlayerSpeed(50, 0);
            desireDirection = 1;
        }
        if (btnLeft) {
            addPlayerSpeed(-50, 0);
            desireDirection = -1;
        }

        if (btnUp){
            if (jumpTimeLeftMs > 0){
                jumpTimeLeftMs -= ms;
                vy = -900;
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
        vy += dy;
        vx += dx;
    }

    private boolean btnAction, btnUp, btnDown, btnRight, btnLeft;

    @Override
    public void draw(@NotNull Camera camera) {
        double dx = Math.abs(px - lastFramePx);
        lastFramePx = px;

        Animation a = desireDirection > 0 ? run_right : run_left;

        a.advance((int) dx); // animate based on movement

        updateHitBox();

        camera.drawBitmap(spriteSheet.toad, a.rect(), hitBox);
    }

    private void updateHitBox() {
        hitBox.bottom = (int) ((int) py + radius);
        hitBox.top = hitBox.bottom - (28 * 4);
        hitBox.left = (int) px - (int) radius;
        hitBox.right = (int) px + (int) radius;
    }
}
