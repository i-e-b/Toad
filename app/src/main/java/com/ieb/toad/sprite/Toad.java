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

    private int desireDirection = 1; // negative = left, positive = right.
    private long jumpTimeLeftMs;
    private double lastFramePx;
    private final SpriteSheet spriteSheet;

    /** Load Toad graphics */
    public Toad(final SpriteSheet spriteSheet) {
        this.spriteSheet = spriteSheet;
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

        hitBox.bottom = (int) ((int) p1y + radius);
        hitBox.top = hitBox.bottom - (28 * 4);
        hitBox.left = (int) p1x - (int) radius;
        hitBox.right = (int) p1x + (int) radius;

        camera.drawBitmap(spriteSheet.toad, a.rect(), hitBox);
    }
}
