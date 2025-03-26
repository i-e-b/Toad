package com.ieb.toad.world.constraints;

import com.ieb.toad.world.core.Constraint;
import com.ieb.toad.world.core.Thing;

/** Constrain two things to have a fixed distance between their centres */
public class FixedLength extends Constraint {

    private final Thing a;
    private final Thing b;
    private final double length;

    /** Constrain two things to have a fixed distance between their centres */
    public FixedLength (Thing a, Thing b, double length){
        this.a = a;
        this.b = b;
        this.length = length;

        a.linkConstraint(this);
        b.linkConstraint(this);
    }

    @Override
    public void apply() {
        double dx = a.px - b.px;
        double dy = a.py - b.py;
        double d2 = (dx * dx) + (dy * dy);
        double rs = length;

        if (Math.abs(d2 - (rs * rs)) < 1.0) return;

        double d = Math.sqrt(d2); // current distance between centres
        if (Math.abs(d - rs) < 1.0) return;

        double dd = rs - d; // overlap distance
        double frac = (dd / d); // fraction of current distance (dx,dy) to adjust
        double ms = a.mass + b.mass;

        // push apart based on mass
        a.px += dx * frac * (b.mass / ms);
        a.py += dy * frac * (b.mass / ms);
        b.px -= dx * frac * (a.mass / ms);
        b.py -= dy * frac * (a.mass / ms);
    }

    @Override
    public void unlink() {
        a.unlinkConstraint(this);
        b.unlinkConstraint(this);
    }
}
