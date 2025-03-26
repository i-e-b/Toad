package com.ieb.toad.world.core;

public interface SimulationManager {

    /** Return what is at x,y on this level. Returns one of `Collision` */
    int hitTest(double x, double y);

    /** add a constraint to the simulation */
    void addConstraint(Constraint c);

    /** unlink and remove a constraint from the simulation. No effect if constraint is not present. */
    void removeConstraint(Constraint c);
}
