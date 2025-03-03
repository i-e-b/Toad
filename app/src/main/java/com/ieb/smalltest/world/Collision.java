package com.ieb.smalltest.world;

public abstract class Collision {
    /** free space */
    public static final int NULL = 0;

    /** Immovable wall */
    public static final int WALL = 1;

    /** A 'bad guy' */
    public static final int CREEP = 2;

    /** An AI sensor */
    public static final int SENSOR = 4;

    // ...

    /** player hit box */
    public static final int PLAYER = 1024;


    public static boolean hasWall(int hit){
        return (hit & WALL) != 0;
    }
}
