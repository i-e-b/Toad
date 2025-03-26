package com.ieb.toad.world.core;

/**
 * Base for constraint types.
 */
public abstract class Constraint {
    /** Apply this constraint to its linked objects.
     * This is allowed to make direct changes to linked objects. */
    public abstract void apply();

    /** This constraint is being lost. Remove any references to  */
    public abstract void unlink();
}
