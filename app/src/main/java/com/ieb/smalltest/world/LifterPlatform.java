package com.ieb.smalltest.world;

import android.graphics.Paint;
import android.graphics.Rect;

import org.jetbrains.annotations.NotNull;

public class LifterPlatform extends Thing {

    private final double speed;

    public LifterPlatform(int left, int top, int width, int height, double speed) {
        hitBox = new Rect(left, top, left+width, top+height);
        this.speed = speed;
        type = Collision.WALL;
        mass = 100;
        radius = -1; // only the target of collision
        elasticity = 0.1;
        drag = 1.0; // no movement
        gravity = 0.0; // float in space
    }

    @Override
    public void draw(@NotNull Camera camera, Paint paint) {
        paint.setARGB(200, 100,120, 200);
        camera.drawRect(hitBox, paint);

        paint.setARGB(50,0,255,255);
        camera.drawCircle((float)p1x, (float)p1y, (float)hitBox.width()*4, paint);
    }

    @Override
    public void preImpactTest(Thing other) {
        // Set radius to make this interactive. Will be reset after impact resolved
        radius = this.hitBox.width() * 4; // big radius to make falling off less likely

        double offsetY = other.p0y + other.radius + radius - 1; // subtract 1 so we have an overlap to do the pushing

        // Place our collider *under* other's circle, in the middle of our hitbox
        p0x = p1x = hitBox.left + (hitBox.width() / 2.0);
        p0y = p1y = clamp(offsetY, hitBox.top + radius, hitBox.bottom + radius);


        // set velocity up to bump the player
        v0y = -speed;// + (other.p0x - p0x);
    }

    @Override
    public void postImpactResolve(Thing other, boolean impacted) {
        radius = -1.0;
    }

    private double clamp(double v, double min, double max) {
        return Math.min(Math.max(v, min), max);
    }
}
