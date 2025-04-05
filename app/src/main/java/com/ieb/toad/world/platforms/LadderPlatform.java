package com.ieb.toad.world.platforms;

import android.graphics.Rect;

import com.ieb.toad.world.core.Collision;
import com.ieb.toad.world.core.Thing;

public class LadderPlatform extends Thing {

    /** Hit box relative to the world */
    public Rect hitBox;

    public LadderPlatform(int left, int top, int width, int height) {
        hitBox = new Rect(left, top, left+width, top+height);
        type = Collision.WALL | Collision.PASS_THROUGH;
        mass = 10;
        radius = -1; // only the target of collision
        elasticity = 0.2;
        drag = 1.0; // no movement
        gravity = 0.0; // float in space
    }

    @Override
    public void preImpactTest(Thing other) {
        if (other.type != Collision.PLAYER) return; // only collide with player

        // Set radius to make this interactive. Will be reset after impact resolved
        radius = 1.0;

        // Find the closest point to the circle within the rectangle
        px = clamp(other.px, this.hitBox.left+1, this.hitBox.right-1);
        py = clamp(other.py, this.hitBox.top+1, this.hitBox.bottom-1);
        vx = other.px - px;
        vy = other.py - py;
    }

    @Override
    public void postImpactTest() {
        radius = -1.0;
    }
}
