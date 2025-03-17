package com.ieb.toad.world;

import android.graphics.Rect;

import com.ieb.toad.world.core.Camera;
import com.ieb.toad.world.core.Collision;
import com.ieb.toad.world.core.Thing;

import org.jetbrains.annotations.NotNull;

public class LifterPlatform extends Thing {

    private final double speed;

    public LifterPlatform(int left, int top, int width, int height, double speed) {
        hitBox = new Rect(left, top, left+width, top+height);
        this.speed = speed;
        type = Collision.WALL; // so creeps will walk through lifters
        mass = 100;
        radius = -1; // only the target of collision
        elasticity = 0.1;
        drag = 1.0; // no movement
        gravity = 0.0; // float in space
    }

    @Override
    public void think(Level level, int ms) {}

    @Override
    public void draw(@NotNull Camera camera) {
        camera.setARGB(200, 100,120, 200);
        camera.drawRect(hitBox);
    }

    @Override
    public void preImpactTest(Thing other) {
        // Set radius to make this interactive. Will be reset after impact resolved
        radius = this.hitBox.width() * 4; // big radius to make falling off less likely

        double offsetY = other.py + other.radius + radius - 1; // subtract 1 so we have an overlap to do the pushing

        // Place our collider *under* other's circle, in the middle of our hitbox
        px = p1x = hitBox.left + (hitBox.width() / 2.0);
        py = p1y = clamp(offsetY, hitBox.top + radius, hitBox.bottom + radius);


        // set velocity up to bump the player
        vy = -speed;// + (other.p0x - p0x);
    }

    @Override
    public void postImpactResolve(Thing other, boolean impacted) {
        radius = -1.0;
    }

    private double clamp(double v, double min, double max) {
        return Math.min(Math.max(v, min), max);
    }
}
