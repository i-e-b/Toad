package com.ieb.toad.world.core;

import com.ieb.toad.world.Level;

import org.jetbrains.annotations.NotNull;

/** Represents a physical object in a level.
 * Masses are kg, distances are 32px per metre. Time is seconds. */
public abstract class Thing {

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
    protected double ax;

    /** Y acceleration of the object */
    protected double ay;

    /** X acceleration last iteration */
    public double a0x;

    /** Y acceleration last iteration */
    public double a0y;

    /** Render this thing */
    public abstract void draw(@NotNull Camera camera);

    /** Perform any AI functions. This is called once per 10 physics frames */
    public abstract void think(Level level, int ms);

    /**
     * Do any updates before an impact is tested and resolved.
     * This allows updates to position to make a virtual impact point for complex shapes.
     * @param other a nearby object
     */
    public void preImpactTest(Thing other) {
    }

    /**
     * Do any updates after an impact is tested and resolved.
     * This allows updates based on virtual impact point for complex shapes.
     * You should reset any virtual changes here.
     *
     * @param other    a nearby object
     * @param impacted `true` if this and other made contact
     */
    public void postImpactResolve(Thing other, boolean impacted) {
    }


    protected double clamp(double v, double min, double max) {
        return Math.min(Math.max(v, min), max);
    }
}
