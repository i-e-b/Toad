package com.ieb.toad.world.platforms;

import android.graphics.Rect;

import com.ieb.toad.world.core.Camera;
import com.ieb.toad.world.core.Collision;
import com.ieb.toad.world.core.SimulationManager;
import com.ieb.toad.world.core.Thing;

import org.jetbrains.annotations.NotNull;

/** Platform that is only solid from the top */
public class OneWayPlatform extends Thing {

    /** Hit box relative to the world */
    public Rect hitBox;

    public OneWayPlatform(int left, int top, int width, int height) {
        hitBox = new Rect(left, top, left+width, top+height);
        type = Collision.WALL;
        mass = 10;
        radius = -1; // only the target of collision
        elasticity = 0.2;
        drag = 1.0; // no movement
        gravity = 0.0; // float in space
    }

    @Override
    public int think(SimulationManager level, int ms) {return KEEP;}

    @Override
    public void draw(@NotNull Camera camera) {
        //camera.setARGB(128, 0,0, 170);
        //camera.drawRect(hitBox);
    }

    @Override
    public void preImpactTest(Thing other) {
        // If the other thing is below the top surface, have no impact
        if (other.py > hitBox.top) return;

        // If the other is travelling up, don't interact yet
        if (other.vy < 0 ) return;

        // Find the closest point to the circle within the rectangle
        px = clamp(other.px, this.hitBox.left+1, this.hitBox.right-1);
        py = clamp(other.py, this.hitBox.top+1, this.hitBox.bottom-1);
        radius = 1.0; // will be reset after impact resolved

        vx = 0;
        vy = 0;
    }

    @Override
    public void postImpactTest() {
        radius = -1.0;
    }
}
