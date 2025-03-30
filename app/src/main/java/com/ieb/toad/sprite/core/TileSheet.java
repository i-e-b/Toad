package com.ieb.toad.sprite.core;

import android.graphics.Bitmap;
import android.graphics.Rect;

/** Image with a set of regularly sized and spaced square tiles */
public class TileSheet {

    public final Bitmap bitmap;

    public final Rect[] tiles;
    public final int pixelSize;
    private final int width; // count of tiles wide

    public TileSheet(Bitmap src, int pxSize, int offset, int width, int height){
        bitmap = src;
        pixelSize = pxSize;

        this.width = width;
        tiles = new Rect[width * height];

        int i = 0;
        for (int y = 0; y < height; y++){
            int top = ((y+1)*offset) + (pxSize*y);
            for (int x = 0; x < width; x++){
                int left = ((x+1)*offset) + (pxSize*x);
                tiles[i++] = new Rect(left,top, left+pxSize, top+pxSize);
            }
        }
    }

    /** Get the tile rectangle at an x,y co-ord */
    public Rect at(int x, int y){
        return tiles[y*width + x];
    }
}
