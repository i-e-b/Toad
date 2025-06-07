package com.ieb.toad.world.core;

/** Collision types. Each `Thing` should have only one, but they can be used as flags when detecting multiple hits. */
public abstract class Collision {
    /** free space */
    public static final int NULL = 0;

    /** Immovable wall */
    public static final int WALL = 1;

    /** A 'bad guy' */
    public static final int CREEP = 2;

    /** Bullets, thrown creeps, fireballs, etc */
    public static final int BULLET = 4;

    /** An AI sensor */
    public static final int SENSOR = 8;

    /** Do NOT do collision effects (bouncing/pushing etc) */
    public static final int PASS_THROUGH = 16;

    /** A door, jar-top, portal, etc. Checks player input and changes position */
    public static final int DOOR = 32;


    // ...

    /** player hit sensitive areas */
    public static final int PLAYER = 1024;


    public static boolean hasWall(int hit){return (hit & WALL) != 0;}

    public static boolean hasDoor(int hit){return (hit & DOOR) != 0;}

    public static boolean isEmpty(int hit) {return hit == NULL;}

    public static boolean hasPlayer(int hit) {return (hit & PLAYER) != 0;}

    public static boolean hasCreep(int hit) {return (hit & CREEP) != 0;}

    public static boolean hasBullet(int hit) {return (hit & BULLET) != 0;}
}
