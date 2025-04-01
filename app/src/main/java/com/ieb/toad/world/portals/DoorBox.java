package com.ieb.toad.world.portals;

import android.graphics.Rect;

import com.ieb.toad.input.VirtualGamepad;
import com.ieb.toad.world.core.Camera;
import com.ieb.toad.world.core.Collision;
import com.ieb.toad.world.core.SimulationManager;
import com.ieb.toad.world.core.Thing;

import org.jetbrains.annotations.NotNull;

/**
 * Doors are non-colliding objects.
 * When the player presses "up" while touching one, they
 * swap between doors with the same target name, by ID.
 */
public class DoorBox extends DoorThing {
    private final Rect hitBox;

    private boolean triggered, onHold;

    public DoorBox(int left, int top, int width, int height, String target, int objId) {
        super(target, objId);
        hitBox = new Rect(left, top, left+width, top+height);

        type = Collision.DOOR + Collision.PASS_THROUGH;
        radius = 10;
        px = hitBox.centerX();
        py = hitBox.centerY();
        triggered = false;
        onHold = false;
    }

    @Override
    public void draw(@NotNull Camera camera) {

    }

    public int think(SimulationManager level, int ms) {
        if (!triggered) return KEEP;

        // switch location
        triggered = false;
        level.moveNextDoor(target, objId);
        return KEEP;
    }

    @Override
    public void hold() {
        triggered = false;
        onHold = true;
    }

    @Override
    public void preImpactTest(Thing other) {
        if (onHold) { // Don't trigger until 'up' is released
            onHold = VirtualGamepad.isUp();
            return;
        }

        if (other.type != Collision.PLAYER) return;

        if (other.px < hitBox.left || other.px > hitBox.right) return;
        if (other.py < hitBox.top || other.py > hitBox.bottom) return;

        if (VirtualGamepad.isUp()) triggered = true; // handled in `think()`
    }
}
