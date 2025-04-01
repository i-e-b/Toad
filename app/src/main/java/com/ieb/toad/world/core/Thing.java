package com.ieb.toad.world.core;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/** Represents a physical object in a level.
 * Masses are kg, distances are 32px per metre. Time is seconds. */
public abstract class Thing {

    /** Returned by `think`. This thing should be removed from the simulation */
    public static final int REMOVE = -1;

    /** Returned by `think`. This thing should remain in the simulation */
    public static final int KEEP = 0;

    /** Type of this thing. Should be one of `world.Collision` */
    public int type;

    /** Gravity ratio: 0.0 to 1.0; At zero, object floats, at 1.0 object falls normally. */
    public double gravity = 1.0;

    /** Drag coefficient. 0.0 = no drag, 1.0 = total stop */
    public double drag = 0.01;

    /** Energy returned in a bounce: 0.0 to 1.0; At zero, objects stop immediately, at 1.0 objects bounce forever.
     * 0.1 is good for players/enemies; 0.25 is good for thrown stuff. */
    public double elasticity = 0.9;

    /** Maximum allowed velocity. If requested or simulated velocity goes above this, we will restrict it. */
    public double terminalVelocity = 3000.0;

    /** Radius of hit circle. If radius is negative, there is no inter-object collision */
    public double radius;

    /** relative mass of object */
    public double mass = 1.0;

    /** X position of the object */
    public double px;

    /** Y position of the object */
    public double py;

    /** X velocity of the object */
    public double vx;

    /** Y velocity of the object */
    public double vy;

    /** X acceleration of the object */
    public double ax;

    /** Y acceleration of the object */
    public double ay;

    /** X acceleration last iteration */
    protected double a0x;

    /** Y acceleration last iteration */
    protected double a0y;

    /** List of constraints linked to this Thing.
     * This is for reference; constraints are applied from the Simulator
     * using the level's complete constraint list.
     */
    protected List<Constraint> constraints;

    /** Render this thing */
    public abstract void draw(@NotNull Camera camera);

    /** Perform any AI functions. This is called once per 10 physics frames.
     * Should return KEEP or REMOVE */
    public int think(SimulationManager level, int ms) {return KEEP;}

    /**
     * Do any updates before an impact is tested and resolved.
     * This allows updates to position to make a virtual impact point for complex shapes.
     * @param other a nearby object
     */
    public void preImpactTest(Thing other) {
    }

    /**
     * You should reset any virtual changes made in `preImpactTest` here.
     */
    public void postImpactTest() {
    }

    /**
     * Do any updates after an impact is detected
     * This allows updates based on virtual impact point for complex shapes.
     * This is called even if no impact took place.
     *
     * @param other    a nearby object
     * @param impacted `true` if this and other made contact
     */
    public void impactResolve(SimulationManager level, Thing other, boolean impacted) {
    }


    protected double clamp(double v, double min, double max) {
        return Math.min(Math.max(v, min), max);
    }


    /** Returns true only if this thing COULD land on the other */
    public boolean canLandOnTop(Thing other){
        return !((this.py + this.radius - 1) > (other.py - other.radius + 1)); // must be above
        //return !(this.vy - other.vy < 0); // true if going down relative to other
    }

    /** bottom most edge */
    public double bottom(){return py + radius;}
    /** top most edge */
    public double top(){return py - radius;}
    /** right most edge */
    public double right(){return px + radius;}
    /** left most edge */
    public double left(){return px - radius;}

    /** Link a constraint to this Thing, for use with tracking */
    public void linkConstraint(Constraint c) {
        if (constraints == null) constraints = new ArrayList<>(4);
        constraints.add(c);
    }

    /** Remove a link to a constraint to this Thing, for use with tracking */
    public void unlinkConstraint(Constraint c) {
        if (constraints == null) return;
        constraints.remove(c);
    }

    /** Returns true if there are any constraints linked to this thing */
    public boolean anyConstraints(){
        if (constraints == null) return false;
        return !constraints.isEmpty();
    }

    /** Returns list of linked constraints. May be null */
    public List<Constraint> linkedConstraints() {
        return constraints;
    }
}
