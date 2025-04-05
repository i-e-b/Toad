package com.ieb.toad.world.constraints;

import com.ieb.toad.world.core.Constraint;
import com.ieb.toad.world.core.Thing;

/** Try to keep one thing on top of another.
 * Used for player standing on a moving object */
public class StandingOnCreep extends Constraint {

    private final Thing top;
    private final Thing bottom;
    private final double sqrDistLimit;

    /** Try to keep one thing on top of another.
     * Used for player standing on a moving object */
    public StandingOnCreep(Thing top, Thing bottom, double breakDistance){
        this.top = top;
        this.bottom = bottom;

        double limit = top.radius + bottom.radius + breakDistance;
        sqrDistLimit = limit * limit;

        top.linkConstraint(this);
        bottom.linkConstraint(this);
    }

    @Override
    public int apply() {
        double dx = bottom.px - top.px;
        double dy = bottom.py - top.py;
        double sqrDist = (dx*dx)+(dy*dy);

        if (sqrDist > sqrDistLimit) return BROKEN;

        // Keep py at exact distance. Accelerate 'top' toward centre of 'bottom'
        top.py = bottom.py - (top.radius + bottom.radius);
        top.vy = 0;

        // one-way spring:
        top.ax = Math.min(Math.max(dx * dx * dx * dx * dx, -700), 700);

        return OK;
    }

    @Override
    public void unlink() {
        top.unlinkConstraint(this);
        bottom.unlinkConstraint(this);
    }

}
