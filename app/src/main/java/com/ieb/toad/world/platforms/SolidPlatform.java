package com.ieb.toad.world.platforms;

import android.graphics.Rect;

import com.ieb.toad.world.core.Collision;
import com.ieb.toad.world.core.Thing;

public class SolidPlatform extends Thing {

    /** Hit box relative to the world */
    public Rect hitBox;

    public SolidPlatform(int left, int top, int width, int height) {
        hitBox = new Rect(left, top, left+width, top+height);
        type = Collision.WALL;
        mass = 10;
        radius = -1; // only the target of collision
        elasticity = 0.2;
        drag = 1.0; // no movement
        gravity = 0.0; // float in space
    }

    @Override
    public void preImpactTest(Thing other) {
        if (other.type == Collision.WALL) return; // don't collide with other walls

        // Set radius to make this interactive. Will be reset after impact resolved
        radius = 1.0;

        // Find the closest point to the circle within the rectangle
        px = clamp(other.px, hitBox.left+1, hitBox.right-1);
        py = clamp(other.py, hitBox.top+1, hitBox.bottom-1);

        // Match incoming object (equivalent to 100% rigid body)
        mass = other.mass;
        elasticity = other.elasticity;

        double ot = other.py - other.radius;
        double ob = other.py + other.radius;
        double ol = other.px - other.radius;
        double or = other.px + other.radius;
        vx = (ot > hitBox.bottom || ob < hitBox.top) ? other.vx : -other.vx;
        vy = (ol > hitBox.right || or < hitBox.left) ? other.vy : -other.vy;
    }

    @Override
    public void postImpactTest() {
        radius = -1.0;
    }
}
