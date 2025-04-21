package com.ieb.toad.world.constraints;

import com.ieb.toad.world.core.Constraint;
import com.ieb.toad.world.core.Thing;

public class StandingOnGround extends Constraint {
    private final Thing top;
    private final Thing ground;
    private final double diffHeight;

    /** Try to keep one thing on top of another.
     * Used for player standing on a moving object */
    public StandingOnGround(Thing top, Thing ground){
        this.top = top;
        this.ground = ground;

        diffHeight = top.radius + 2;

        top.linkConstraint(this);
    }

    @Override
    public int apply(double timeMs) {
        // Make sure the ground is under us
        ground.preImpactTest(top);

        double adx = Math.abs(ground.px - top.px);
        double dy = ground.py - top.py;

        // clean up
        ground.postImpactTest();

        if (dy > diffHeight) return BROKEN; // off surface from the top

        if (adx > 10 || dy < -5) return BROKEN; // off end, with coyote time
        return OK;
    }

    @Override
    public void unlink() {
        top.unlinkConstraint(this);
    }
}
