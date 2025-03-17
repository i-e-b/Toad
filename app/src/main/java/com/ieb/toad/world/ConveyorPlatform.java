package com.ieb.toad.world;

import android.graphics.Rect;

import com.ieb.toad.world.core.Camera;
import com.ieb.toad.world.core.Collision;
import com.ieb.toad.world.core.Thing;

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
    }

    @Override
    public void think(Level level, int ms) {}

    @Override
    public void preImpactTest(Thing other) {
        // Find the closest point to the circle within the rectangle
        px = p1x = clamp(other.px, this.hitBox.left + 1, this.hitBox.right - 1);
        py = p1y = clamp(other.py, this.hitBox.top + 1, this.hitBox.bottom - 1);

        // Set radius to make this interactive. Will be reset after impact resolved
        radius = (other.py > this.hitBox.bottom) ? 32.0 : 1.0; // TODO: remove this hack when joints are done

        vy = other.py - py;
        if (other.py > hitBox.top) { // normal impact from below
            vx = other.px - px;
        } else { // moving impact from above
            px -= speed;
            p1x -= speed;
            vx = speed;// + (other.p0x - p0x);
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
