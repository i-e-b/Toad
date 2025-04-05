package com.ieb.toad.world.constraints;

import com.ieb.toad.world.core.Constraint;
import com.ieb.toad.world.core.Thing;

/** Try to keep one thing on top of another.
 * Used for player standing on a moving object */
public class StandingOnCreep extends Constraint {
    private final Thing top;
    private final Thing bottom;
    private final double diffHeight;
    private final double diffWidth;

    /** Try to keep one thing on top of another.
     * Used for player standing on a moving object */
    public StandingOnCreep(Thing top, Thing bottom){
        this.top = top;
        this.bottom = bottom;

        diffHeight = top.radius + bottom.radius + 8;
        diffWidth = top.radius + bottom.radius - 4;

        top.linkConstraint(this);
        bottom.linkConstraint(this);
    }

    @Override
    public int apply() {
        double dx = bottom.px - top.px;
        double dy = bottom.py - top.py;

        if (dx > diffWidth || dx < -diffWidth) return BROKEN;
        if (dy < -1.5 || dy > diffHeight) return BROKEN;

        // one-way spring, only affects 'top'
        top.ax = Math.min(Math.max(dx * dx * dx * dx * dx, -700), 700);
        top.ay = Math.min(Math.max(dy * dy * dy * dy * dy, -700), 700);

        return OK;
    }

    @Override
    public void unlink() {
        top.unlinkConstraint(this);
        bottom.unlinkConstraint(this);
    }

}
