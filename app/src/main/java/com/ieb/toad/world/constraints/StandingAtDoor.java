package com.ieb.toad.world.constraints;

import com.ieb.toad.world.core.Constraint;
import com.ieb.toad.world.core.Thing;
import com.ieb.toad.sprite.Key;
import com.ieb.toad.world.portals.DoorBox;

/** Link player to a door. Link breaks if distance is exceeded. */
public class StandingAtDoor extends Constraint {

    public final Thing toad;
    public final DoorBox door;
    private final double sqrDistance;

    /** Link player to a door. Link breaks if distance is exceeded. */
    public StandingAtDoor(Thing toad, DoorBox door){
        this.toad = toad;
        this.door = door;

        double distance = toad.radius + door.radius;
        sqrDistance = distance * distance;

        toad.linkConstraint(this);
        door.linkConstraint(this);
    }

    @Override
    public int apply(double timeMs) {
        double dx = toad.px - door.px;
        double dy = toad.py - door.py;
        double d2 = (dx * dx) + (dy * dy);

        if (d2 < sqrDistance) return OK;
        return BROKEN;
    }

    @Override
    public void unlink() {
        toad.unlinkConstraint(this);
        door.unlinkConstraint(this);
    }

    /** Player has tried to use the door. Returns true is door worked. */
    public boolean travel(Key key) {
        return door.trigger(key);
    }
}
