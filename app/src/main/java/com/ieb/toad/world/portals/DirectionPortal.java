package com.ieb.toad.world.portals;

import android.graphics.Rect;

import com.ieb.toad.world.core.Collision;
import com.ieb.toad.world.core.Direction;
import com.ieb.toad.world.core.SimulationManager;
import com.ieb.toad.world.core.Thing;

public class DirectionPortal extends DoorThing {
    private final Rect hitBox;
    private final int triggerDirections;

    private boolean triggered, onHold;

    public DirectionPortal(int left, int top, int width, int height, String target, int direction, int objId) {
        super(target, objId);

        triggerDirections = direction;
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
        triggered = false;
        onHold = true;

        t.px = hitBox.centerX();
        t.py = hitBox.centerY();
        t.vx = 0;
        t.vy = 0;
    }

    @Override
    public void preImpactTest(Thing other) {
        // Only trigger if other is player inside our hit box
        if (other.type != Collision.PLAYER) return;
        if (other.px < hitBox.left || other.px > hitBox.right) return;
        if (other.py < hitBox.top || other.py > hitBox.bottom) return;

        // Check if player direction matches our triggers
        int dirs = 0;
        if (other.vy < -0.05) dirs += Direction.UP;
        if (other.vy >  0.05) dirs += Direction.DOWN;
        if (other.vx < -0.05) dirs += Direction.LEFT;
        if (other.vx >  0.05) dirs += Direction.RIGHT;

        boolean meetsTrigger = (dirs & this.triggerDirections) != 0;

        // Fire trigger if appropriate
        if (onHold) { // Don't trigger until trigger condition stops
            onHold = meetsTrigger;
            return;
        }
        triggered = meetsTrigger;
    }
}