package com.ieb.toad.world.core;

import android.graphics.Rect;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

/** Represents a physical object in a level.
 * Masses are kg, distances are 32px per metre. Time is seconds. */
public abstract class Thing {

    /** Returned by `think`. This thing should be removed from the simulation */
    public static final int REMOVE = -1;

    /** Returned by `think`. This thing should remain in the simulation */
    public static final int KEEP = 0;

    /** Returned by `preImpactTest` if this thing would like to interact with another */
    public static final boolean DO_IMPACT = true;

    /** Returned by `preImpactTest` if this thing should not interact with the other */
    public static final boolean SKIP_IMPACT = false;

    /** Type of this thing. Should be one of `world.Collision` */
    public int type;

    /** Default value for gravity fraction */
    public static final double DEFAULT_GRAVITY = 1.0;

    /** Gravity ratio: 0.0 to 1.0; At zero, object floats, at 1.0 object falls normally. */
    public double gravity = DEFAULT_GRAVITY;

    /** Default value for drag */
    public static final double DEFAULT_DRAG = 0.01;

    /** Drag coefficient. 0.0 = no drag, 1.0 = total stop. Drag is very strong, and should normally be under 0.1 */
    public double drag = DEFAULT_DRAG;

    /** Default value for elasticity */
    public static final double DEFAULT_ELASTICITY = 0.9;

    /** Energy returned in a bounce: 0.0 to 1.0; At zero, objects stop immediately, at 1.0 objects bounce forever.
     * 0.1 is good for players/enemies; 0.25 is good for thrown stuff. */
    public double elasticity = DEFAULT_ELASTICITY;

    /** Default value for terminal velocity */
    public static final double DEFAULT_TERMINAL_VELOCITY = 3000.0;

    /** Maximum allowed velocity. If requested or simulated velocity goes above this, we will restrict it. */
    public double terminalVelocity = DEFAULT_TERMINAL_VELOCITY;

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
    protected HashSet<Constraint> constraints;

    /** [Optional Override]
     * Render this thing */
    public void draw(@NotNull Camera camera, int frameMs){}

    /** [Optional Override]
     * Perform any AI functions. This is called once per 10 physics frames.
     * Should return KEEP or REMOVE */
    public int think(SimulationManager level, int ms) {return KEEP;}

    /** [Optional Override]
     * Do any updates before an impact is tested and resolved.
     * This allows updates to position to make a virtual impact point for complex shapes.
     * @param other a nearby object
     * @return true if impact should be run, false if this and other should ignore each other
     */
    public boolean preImpactTest(Thing other) {return DO_IMPACT;}

    /** [Optional Override]
     * You should reset any virtual changes made in `preImpactTest` here.
     */
    public void postImpactTest() {
    }

    /** [Optional Override]
     * Do any updates after an impact is detected
     * This allows updates based on virtual impact point for complex shapes.
     * This is called even if no impact took place.
     *
     * @param other    a nearby object
     * @param impacted `true` if this and other made contact
     */
    public void impactResolve(SimulationManager level, Thing other, boolean impacted) {
    }

    /** [Optional Override]
     * Perform any actions or checks when a constrain is first added
     */
    protected void constrainAdded(Constraint c) {}

    /** [Optional Override]
     * Perform any actions or checks when a constrain is first removed
     */
    protected void constrainRemoved(Constraint c) {}

    protected final double clamp(double v, double min, double max) {
        return Math.min(Math.max(v, min), max);
    }


    /** Returns true only if this thing COULD land on the other */
    public final boolean canLandOnTop(Thing other){
        double bottomOfThis = py + radius - (Simulator.h * vy) - 1;
        double topOfOther = other.py - other.radius + (Simulator.h * other.vy) + 1;
        return bottomOfThis <= topOfOther;
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
    public final void linkConstraint(Constraint c) {
        if (constraints == null) constraints = new HashSet<>();
        if (constraints.add(c)) constrainAdded(c);
    }

    /** Remove a link to a constraint to this Thing, for use with tracking */
    public final void unlinkConstraint(Constraint c) {
        if (constraints == null) return;
        if (constraints.remove(c)) constrainRemoved(c);
    }

    /** Returns true if there are any constraints linked to this thing */
    public final boolean anyConstraints(){
        if (constraints == null) return false;
        return !constraints.isEmpty();
    }

    /** Returns true if there are any constraints linked to this thing */
    public final boolean hasConstraint(Class<? extends Constraint> type){
        if (constraints == null) return false;
        for (Constraint c : constraints) {
            Class<? extends Constraint> cType = c.getClass();
            if (cType == type) return true;
        }
        return false;
    }

    /** Returns true if there are any constraints linked to this thing */
    public final Constraint getConstraint(Class<? extends Constraint> type){
        if (constraints == null) return null;
        for (Constraint c : constraints) {
            Class<? extends Constraint> cType = c.getClass();
            if (cType == type) return c;
        }
        return null;
    }

    /** Returns list of linked constraints. May be empty, but won't be null.
     * DO NOT modify this collection */
    public final Iterable<Constraint> linkedConstraints() {
        if (constraints == null) return emptyConstraints;
        return constraints;
    }
    private static final Collection<Constraint> emptyConstraints = new LinkedList<>();

    /** get bounds of impact circle */
    public Rect boundBox(){
        return new Rect((int) (px-radius), (int) (py-radius), (int) (px+radius), (int) (py+radius));
    }
}
