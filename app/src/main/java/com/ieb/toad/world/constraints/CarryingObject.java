package com.ieb.toad.world.constraints;

import com.ieb.toad.world.core.Constraint;
import com.ieb.toad.world.core.Thing;

/** Try to keep one thing on top of another.
 * Used for player carrying an object */
public class CarryingObject extends Constraint {
    public final Thing carried;
    public final Thing holder;
    private final double targetHeight;
    private final double time;
    private double nowHeight;

    /** Try to keep one thing on top of another.
     * Used for player standing on a moving object */
    public CarryingObject(Thing carried, Thing holder, double height, double time){
        this.carried = carried;
        this.holder = holder;
        this.targetHeight = height;
        this.time = time;
        nowHeight = 0.0;

        carried.linkConstraint(this);
        holder.linkConstraint(this);
    }

    @Override
    public int apply(double timeMs) {
        if (nowHeight < targetHeight){
            nowHeight += targetHeight * (timeMs / time);
        } else nowHeight = targetHeight;

        carried.px = holder.px;
        carried.py = holder.py - nowHeight;

        return OK;
    }

    @Override
    public void unlink() {
        carried.unlinkConstraint(this);
        holder.unlinkConstraint(this);
    }

}
