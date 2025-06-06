package com.ieb.toad.world.platforms;

import android.graphics.Rect;

import com.ieb.toad.world.core.Collision;
import com.ieb.toad.world.core.Thing;

public class LifterPlatform extends Thing {

    /** Hit box relative to the world */
    public Rect hitBox;

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
    public boolean preImpactTest(Thing other) {
        if (other.type == Collision.WALL) return SKIP_IMPACT; // don't collide with other walls

        // Set radius to make this interactive. Will be reset after impact resolved
        radius = this.hitBox.width() * 4; // big radius to make falling off less likely

        double offsetY = other.py + other.radius + radius - 1; // subtract 1 so we have an overlap to do the pushing

        // Place our collider *under* other's circle, in the middle of our hit-box
        px = hitBox.left + (hitBox.width() / 2.0);
        py = clamp(offsetY, hitBox.top + radius, hitBox.bottom + radius);

        // set velocity up to bump the player
        vy = -speed;

        return DO_IMPACT;
    }

    @Override
    public void postImpactTest() {
        radius = -1.0;
    }
}
