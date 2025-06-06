package com.ieb.toad.world.platforms;

import android.graphics.Rect;

import com.ieb.toad.sprite.Toad;
import com.ieb.toad.world.core.Collision;
import com.ieb.toad.world.core.Thing;

public class LadderPlatform extends Thing {

    /** Hit box relative to the world */
    public Rect hitBox;
    private final double halfWidth;

    public LadderPlatform(int left, int top, int width, int height) {
        hitBox = new Rect(left, top, left+width, top+height);
        type = Collision.WALL | Collision.PASS_THROUGH;
        mass = 10;
        halfWidth = width / 2.0;
        radius = -1; // only the target of collision
        elasticity = 0.2;
        drag = 1.0; // no movement
        gravity = 0.0; // float in space
    }

    @Override
    public boolean preImpactTest(Thing other) {
        if (other.type != Collision.PLAYER) return SKIP_IMPACT; // only collide with player

        if (other.py < hitBox.top) {
            // If the other thing is above the top surface, act like a platform
            // Find the closest point to the circle within the rectangle
            px = clamp(other.px, hitBox.left+1, hitBox.right-1);
            py = clamp(other.py, hitBox.top+1, hitBox.bottom-1);
            radius = 1.0;

            Toad t = (Toad)other;
            type = t.climbing ? Collision.WALL | Collision.PASS_THROUGH : Collision.WALL;

            mass = other.mass;
            elasticity = other.elasticity;
            vx = other.vx;
            vy = -other.vy;
        } else {
            // Set radius to make this interactive. Will be reset after impact resolved
            radius = halfWidth;
            type = Collision.WALL | Collision.PASS_THROUGH;

            // Find the closest point to the circle within the rectangle
            px = hitBox.left + halfWidth;
            py = clamp(other.py, this.hitBox.top+1, this.hitBox.bottom-1);
        }
        return DO_IMPACT;
    }

    @Override
    public void postImpactTest() {
        type = Collision.WALL | Collision.PASS_THROUGH;
        radius = -1.0;
    }
}
