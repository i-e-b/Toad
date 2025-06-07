package com.ieb.toad.world.portals;

import android.graphics.Rect;

import com.ieb.toad.input.VirtualGamepad;
import com.ieb.toad.world.core.Collision;
import com.ieb.toad.world.core.SimulationManager;
import com.ieb.toad.world.core.Thing;

/**
 * Pots are solid wall boxes; if the player
 * presses 'down' when on top of one, it acts
 * as a door to the next portal.
 */
public class PotBox extends DoorThing {
    private final Rect hitBox;

    private boolean triggered, onHold;

    public PotBox(int left, int top, int width, int height, String target, int objId) {
        super(target, objId);
        hitBox = new Rect(left, top, left+width, top+height);

        type = Collision.DOOR + Collision.WALL;
        mass = 10;
        radius = -1; // only the target of collision
        elasticity = 0.2;
        drag = 1.0; // no movement
        gravity = 0.0; // float in space

        triggered = false;
        onHold = false;
    }

    public int think(SimulationManager level, int ms) {
        if (!triggered) return KEEP;

        // switch location
        triggered = false;
        level.moveNextDoor(target, objId);
        return KEEP;
    }

    @Override
    public void movePlayerToDoor(Thing t) {
        onHold = true;
        triggered = false;

        t.px = hitBox.centerX();
        t.py = hitBox.top - t.radius;
        t.vx = 0;
        t.vy = 0;
    }

    @Override
    public boolean preImpactTest(Thing other) {
        if (Collision.hasWall(other.type)) return SKIP_IMPACT; // don't collide with other walls

        // Set radius to make this interactive. Will be reset after impact resolved
        radius = 1.0;

        // Find the closest point to the circle within the rectangle
        px = clamp(other.px, hitBox.left+1, hitBox.right-1);
        py = clamp(other.py, hitBox.top+1, hitBox.bottom-1);

        // Match incoming object (equivalent to 100% rigid body)
        mass = other.mass;
        if (other.py < hitBox.top || other.py > hitBox.bottom){
            vx = other.vx;
            vy = -other.vy; // reflect across horz
        } else {
            vx = -other.vx;  // reflect across vert
            vy = other.vy;
        }
        return DO_IMPACT;
    }

    @Override
    public void postImpactTest() {
        radius = -1.0;
    }

    @Override
    public void impactResolve(SimulationManager level, Thing other, boolean impacted) {
        if (onHold) { // Don't trigger until 'down' is released
            onHold = VirtualGamepad.isDown();
            return;
        }

        // Test: player touching, on top, pressing 'down'
        if (!impacted) return;
        if (other.type != Collision.PLAYER) return;
        if (other.py < hitBox.top && VirtualGamepad.isDown()) triggered = true; // handled in `think()`
    }
}
