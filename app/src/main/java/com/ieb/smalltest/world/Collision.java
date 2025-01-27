package com.ieb.smalltest.world;

public abstract class Collision {
    /** Sensor is in free space */
    public static final int NULL = 0;

    /** Immovable wall */
    public static final int WALL = 1;

    /** A 'bad guy' */
    public static final int CREEP = 2;

    // ...

    /** Sensor is touching a player hit box */
    public static final int PLAYER = 1000;
}
