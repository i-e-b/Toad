package com.ieb.toad.world.constraints;

import com.ieb.toad.world.core.Constraint;
import com.ieb.toad.world.core.Thing;

/** Try to keep one thing on top of another.
 * Used for player carrying an object */
public class CarryingObject extends Constraint {
    public final Thing carried;
    public final Thing holder;
    private final double height;

    /** Try to keep one thing on top of another.
     * Used for player standing on a moving object */
    public CarryingObject(Thing carried, Thing holder, double height){
        this.carried = carried;
        this.holder = holder;
        this.height = height;

        carried.linkConstraint(this);
        holder.linkConstraint(this);
    }

    @Override
    public int apply() {
        carried.px = holder.px;
        carried.py = holder.py - height;

        return OK;
    }

    @Override
    public void unlink() {
        carried.unlinkConstraint(this);
        holder.unlinkConstraint(this);
    }

}
