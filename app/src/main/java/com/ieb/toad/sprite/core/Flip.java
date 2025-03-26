package com.ieb.toad.sprite.core;

/** Image flip flags */
public abstract class Flip {

    /** No flip */
    public static final int None = 0;

    /** Horizontal flip */
    public static final int Horz = 1;

    /** Vertical flip */
    public static final int Vert = 2;

    /** Horizontal and vertical flip */
    public static final int Both = Horz + Vert;
}
