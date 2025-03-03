package com.ieb.smalltest.world;

import android.graphics.Rect;

import org.jetbrains.annotations.NotNull;

public class ConveyorPlatform extends Thing {

    private final double speed;

    public ConveyorPlatform(int left, int top, int width, int height, double speed) {
        hitBox = new Rect(left, top, left+width, top+height);
        this.speed = speed;
        type = Collision.WALL;
        mass = 10;
        radius = -1; // only the target of collision
        elasticity = 0.2;
        drag = 1.0; // no movement
        gravity = 0.0; // float in space
    }

    @Override
    public void draw(@NotNull Camera camera) {
        camera.setARGB(200, 120,100, 100);
        camera.drawRect(hitBox);

        camera.setARGB(120,0,255,255);
        camera.drawCircle((float)p1x, (float)p1y, (float)5.0);
    }

    @Override
    public void think(Level level, int ms) {}

    @Override
    public void preImpactTest(Thing other) {
        // Find the closest point to the circle within the rectangle
        p0x = p1x = clamp(other.p0x, this.hitBox.left + 1, this.hitBox.right - 1);
        p0y = p1y = clamp(other.p0y, this.hitBox.top + 1, this.hitBox.bottom - 1);

        // Set radius to make this interactive. Will be reset after impact resolved
        radius = (other.p0y > this.hitBox.bottom) ? 32.0 : 1.0; // TODO: remove this hack when joints are done

        v0y = other.p0y - p0y;
        if (other.p0y > hitBox.top) { // normal impact from below
            v0x = other.p0x - p0x;
        } else { // moving impact from above
            p0x -= speed;
            p1x -= speed;
            v0x = speed;// + (other.p0x - p0x);
        }
    }

    @Override
    public void postImpactResolve(Thing other, boolean impacted) {
        radius = -1.0;
    }

    private double clamp(double v, double min, double max) {
        return Math.min(Math.max(v, min), max);
    }
}
