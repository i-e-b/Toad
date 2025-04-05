package com.ieb.toad.world.constraints;

import com.ieb.toad.world.core.Constraint;
import com.ieb.toad.world.core.Thing;

public class StandingOnGround extends Constraint {
    private final Thing top;
    private final Thing bottom;
    private final double diffHeight;

    /** Try to keep one thing on top of another.
     * Used for player standing on a moving object */
    public StandingOnGround(Thing top, Thing bottom){
        this.top = top;
        this.bottom = bottom;

        diffHeight = top.radius + 2;

        top.linkConstraint(this);
        bottom.linkConstraint(this);
    }

    @Override
    public int apply() {
        // Make sure the ground is under us
        bottom.preImpactTest(top);

        double adx = Math.abs(bottom.px - top.px);
        double dy = bottom.py - top.py;

        // clean up
        bottom.postImpactTest();

        if (dy > diffHeight) return BROKEN; // off surface from the top

        if (adx > 10 || dy < -5) return BROKEN; // off end, with coyote time
        return OK;
    }

    @Override
    public void unlink() {
        top.unlinkConstraint(this);
        bottom.unlinkConstraint(this);
    }
}
