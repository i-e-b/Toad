package com.ieb.smalltest.sprite;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;

import com.ieb.smalltest.Main;
import com.ieb.smalltest.world.Camera;
import com.ieb.smalltest.world.Collision;
import com.ieb.smalltest.world.Level;
import com.ieb.smalltest.world.Thing;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;

public class Shy extends Thing {
    private final Animation left = new Animation(64, Animation.FOREVER, 16, 16, 0, new int[]{1, 18});
    private final Animation right = new Animation(64, Animation.FOREVER, 16, 16, 186, new int[]{246, 229});

    private double lastFramePx;
    private final SpriteSheet spriteSheet;

    private int desireDirection = -1; // negative = left, positive = right.

    private final int speed = 1200, accel = 5000;

    /** Load Toad graphics */
    public Shy(final SpriteSheet spriteSheet) {
        this.spriteSheet = spriteSheet;
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
        var frontSense = level.hitTest(p0x + ((radius + 2) * desireDirection), p0y);
        if (Collision.hasWall(frontSense)) desireDirection = -desireDirection;

        if (desireDirection < 0){ // left
            if (v0x > -speed) a0x = -accel;
        } else { // right
            if (v0x < speed) a0x = accel;
        }
    }

    @Override
    public void draw(@NotNull Camera camera) {
        double dx = Math.abs(p0x - lastFramePx);
        lastFramePx = p0x;

        Animation a = desireDirection > 0 ? right : left;

        a.advance((int) dx); // animate based on movement

        hitBox.bottom = (int) ((int) p1y + radius);
        hitBox.top = hitBox.bottom - (16 * 4);
        hitBox.left = (int) p1x - (int) radius;
        hitBox.right = (int) p1x + (int) radius;

        camera.drawBitmap(spriteSheet.dude, a.rect(), hitBox);
    }
}
