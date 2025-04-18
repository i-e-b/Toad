package com.ieb.toad.world.portals;

import com.ieb.toad.world.core.Collision;
import com.ieb.toad.world.core.Thing;

/** Base class for doors, portals, etc. Adds object ID and target to a Thing */
public abstract class DoorThing extends Thing {
    public final String target;
    public final int objId;

    public DoorThing(String target, int objId) {
        this.target = target;
        this.objId = objId;

        type = Collision.DOOR;
        mass = 1.0;
        gravity = 0.0;
    }

    /** Indicates that player has just arrived at the door.
     * The door should place the player as appropriate,
     * and wait for trigger control to stop before triggering again. */
    public abstract void moveAndHold(Thing t);
}
