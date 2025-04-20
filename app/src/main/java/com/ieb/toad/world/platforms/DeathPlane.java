package com.ieb.toad.world.platforms;

import android.graphics.Rect;

import com.ieb.toad.world.core.Collision;
import com.ieb.toad.world.core.SimulationManager;
import com.ieb.toad.world.core.Thing;

public class DeathPlane extends Thing {
    /** Hit box relative to the world */
    public Rect hitBox;

    public DeathPlane(int left, int top, int width, int height) {
        hitBox = new Rect(left, top, left+width, top+height);
        type = Collision.WALL + Collision.PASS_THROUGH;
        mass = 10;
        radius = -1; // only the target of collision
        elasticity = 0.2;
        drag = 1.0; // no movement
        gravity = 0.0; // float in space
    }

    @Override
    public void preImpactTest(Thing other) {
        if (other.type != Collision.PLAYER) return; // only collide with player

        radius = 1.0;
        // Find the closest point to the circle within the rectangle
        px = clamp(other.px, hitBox.left+1, hitBox.right-1);
        py = clamp(other.py, hitBox.top+1, hitBox.bottom-1);
    }

    @Override
    public void postImpactTest() {
        radius = -1.0;
    }

    @Override
    public void impactResolve(SimulationManager level, Thing other, boolean impacted) {
        if (!impacted) return;

        if (Collision.hasPlayer(other.type)) level.damagePlayer();
        if (Collision.hasCreep(other.type)) level.killCreep(other);
        if (Collision.hasBullet(other.type)) level.removeThing(other);
    }
}
