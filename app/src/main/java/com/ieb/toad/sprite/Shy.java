package com.ieb.toad.sprite;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import com.ieb.toad.world.core.Camera;
import com.ieb.toad.world.core.Collision;
import com.ieb.toad.world.Level;
import com.ieb.toad.world.core.Thing;

import org.jetbrains.annotations.NotNull;

public class Shy extends Thing {
    private final Animation left;
    private final Animation right;

    private double lastFramePx;

    private int desireDirection = -1; // negative = left, positive = right.

    private final int speed = 1200, accel = 5000;

    /** Load Toad graphics */
    public Shy(final SpriteSheetManager spriteSheetManager) {

        left = new Animation(64, Animation.FOREVER, spriteSheetManager.dude, new int[]{0,1});
        right = new Animation(64, Animation.FOREVER, spriteSheetManager.dude, new int[]{0,1});

        hitBox = new Rect(0,0,0,0);
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        type = Collision.CREEP;
        radius = 32;
        mass = 0.8;
        gravity = 1.0; // fully affected by gravity
    }

    @Override
    public void think(Level level, int ms) {
        // Switch direction if facing wall.
        var frontSense = level.hitTest(px + ((radius + 2) * desireDirection), py);
        if (Collision.hasWall(frontSense)) desireDirection = -desireDirection;

        if (desireDirection < 0){ // left
            if (vx > -speed) a0x = -accel;
        } else { // right
            if (vx < speed) a0x = accel;
        }
    }

    @Override
    public void draw(@NotNull Camera camera) {
        double dx = Math.abs(px - lastFramePx);
        lastFramePx = px;

        Animation anim = desireDirection > 0 ? right : left;

        anim.advance((int) dx); // animate based on movement

        hitBox.bottom = (int) ((int) py + radius);
        hitBox.top = hitBox.bottom - (16 * 4);
        hitBox.left = (int) px - (int) radius;
        hitBox.right = (int) px + (int) radius;

        camera.drawSprite(anim, hitBox);
    }
}
