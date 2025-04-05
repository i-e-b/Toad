package com.ieb.toad.world.loader;

import android.graphics.Rect;

/** Information about a scene in the level. Used to pin the camera, set background, etc */
public class CameraZone {
    /** camera zone. Camera should lock inside this area.
     * If the camera is larger than the rect, overflow to the top and right. */
    public final Rect rect;

    /** background color. Zero equal no specific color, use level default. */
    public int color;

    public CameraZone(int left, int top, int width, int height) {
        rect = new Rect(left,top, left+width, top+height);
    }
}
