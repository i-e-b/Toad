package com.ieb.toad.world.constraints;

import com.ieb.toad.world.core.Constraint;
import com.ieb.toad.world.core.Thing;

public class StandingOnGround extends Constraint {
    private final Thing top;
    private final Thing bottom;
    private final double sqrDistLimit;

    /** Try to keep one thing on top of another.
     * Used for player standing on a moving object */
    public StandingOnGround(Thing top, Thing bottom, double breakDistance){
        this.top = top;
        this.bottom = bottom;

        double limit = top.radius + breakDistance;
        sqrDistLimit = limit * limit;

        top.linkConstraint(this);
        bottom.linkConstraint(this);
    }

    @Override
    public int apply() {
        // Make sure the ground is under us
        bottom.preImpactTest(top);

        double dx = bottom.px - top.px;
        double dy = bottom.py - top.py;
        double sqrDist = (dx*dx)+(dy*dy);

        // clean up
        bottom.postImpactTest();

        if (sqrDist > sqrDistLimit) return BROKEN;
        return OK;
    }

    @Override
    public void unlink() {
        top.unlinkConstraint(this);
        bottom.unlinkConstraint(this);
    }
}
