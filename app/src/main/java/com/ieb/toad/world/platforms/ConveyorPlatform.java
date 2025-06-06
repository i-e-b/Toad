package com.ieb.toad.world.platforms;

import android.graphics.Rect;

import com.ieb.toad.world.core.Collision;
import com.ieb.toad.world.core.Thing;

public class ConveyorPlatform extends Thing {

    /** Hit box relative to the world */
    public Rect hitBox;

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
    public boolean preImpactTest(Thing other) {
        if (other.type == Collision.WALL) return SKIP_IMPACT; // don't collide with other walls

        // Find the closest point to the circle within the rectangle
        px = clamp(other.px, this.hitBox.left + 1, this.hitBox.right - 1);
        py = clamp(other.py, this.hitBox.top + 1, this.hitBox.bottom - 1);

        // Set radius to make this interactive. Will be reset after impact resolved
        radius = 1.0;

        mass = other.mass;
        vy = other.py - py;
        if (other.py > hitBox.top) { // normal impact from below
            vx = other.vx;
        } else { // moving impact from above
            px -= speed;
            vx = speed;
        }
        return DO_IMPACT;
    }

    @Override
    public void postImpactTest() {
        radius = -1.0;
    }
}
