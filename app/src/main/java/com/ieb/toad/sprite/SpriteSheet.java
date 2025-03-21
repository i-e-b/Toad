package com.ieb.toad.sprite;

import android.graphics.Bitmap;
import android.graphics.Rect;

import java.util.List;

/** Holds reference to a bitmap, plus an ordered set of rectangles within it */
public class SpriteSheet {

    public final Bitmap bitmap;

    public final Rect[] tiles;

    public SpriteSheet(Bitmap src, List<Rect> tileList){
        bitmap = src;
        tiles = tileList.toArray(new Rect[0]);
    }
}
