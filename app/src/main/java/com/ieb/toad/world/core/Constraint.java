package com.ieb.toad.world.core;

/**
 * Base for constraint types.
 */
public abstract class Constraint {
    /** Constraint is no longer valid and should be removed */
    public static final int BROKEN = -1;
    /** Constraint is still valid */
    public static final int OK = 0;

    /** Apply this constraint to its linked objects.
     * This is allowed to make direct changes to linked objects.
     *
     * @return one of Constraint.OK, Constraint.BROKEN. If Broken is returned, the constraint is unlinked and removed. */
    public abstract int apply();

    /** This constraint is being lost. Remove any references to  */
    public abstract void unlink();
}
