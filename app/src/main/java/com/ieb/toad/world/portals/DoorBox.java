package com.ieb.toad.world.portals;

import android.graphics.Rect;

import com.ieb.toad.input.VirtualGamepad;
import com.ieb.toad.world.core.Collision;
import com.ieb.toad.world.core.SimulationManager;
import com.ieb.toad.world.core.Thing;

/**
 * Doors are non-colliding objects.
 * When the player presses "up" while touching one, they
 * swap between doors with the same target name, by ID.
 */
public class DoorBox extends DoorThing {
    private final Rect hitBox;
    private boolean locked;

    private boolean triggered, onHold;

    public DoorBox(int left, int top, int width, int height, String target, boolean locked, int objId) {
        super(target, objId);
        this.locked = locked;
        hitBox = new Rect(left, top, left+width, top+height);

        type = Collision.DOOR + Collision.PASS_THROUGH;
        radius = 10;
        px = hitBox.centerX();
        py = hitBox.centerY();
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
    public void moveAndHold(Thing t) {
        onHold = true;
        triggered = false;

        t.px = hitBox.centerX();
        t.py = hitBox.bottom - t.radius;
        t.vx = 0;
        t.vy = 0;
    }

    @Override
    public boolean preImpactTest(Thing other) {
        if (onHold) { // Don't trigger until 'up' is released
            onHold = VirtualGamepad.isUp();
            return SKIP_IMPACT;
        }

        if (other.type != Collision.PLAYER) return SKIP_IMPACT;

        if (other.px < hitBox.left || other.px > hitBox.right) return SKIP_IMPACT;
        if (other.py < hitBox.top || other.py > hitBox.bottom) return SKIP_IMPACT;

        // TODO: move to Toad?
        if (VirtualGamepad.isUp()) triggered = true; // handled in `think()`
        return DO_IMPACT;
    }
}
