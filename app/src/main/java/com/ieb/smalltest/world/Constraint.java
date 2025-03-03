package com.ieb.smalltest.world;

/**
 * Base for constraint types.
 */
public abstract class Constraint {

    /** Apply this constraint to its linked objects.
     * This is allowed to make direct changes to linked objects. */
    public abstract void apply();

    /** This constraint is being lost. Remove any references to  */
    public abstract void unlink();

    // TODO: Start with a fixed length link. Make a creep that walks around. Link when on top.
}
