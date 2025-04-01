package com.ieb.toad.sprite.core;

import android.graphics.Bitmap;
import android.graphics.Rect;

/** Image with a set of regularly sized and spaced square tiles */
public class TileSheet {

    public static final int ANIMATION_THRESHOLD = 520;
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

    /** Get a tile based on index. If in an animated range, this may change based on animationFrame */
    public Rect getTile(int tileIdx, int animationFrame) {
        // Non-animated tiles
        if (tileIdx < ANIMATION_THRESHOLD) return tiles[tileIdx];

        if (tileIdx <= 527) return animRange(tileIdx, 520, animationFrame, 8);
        if (tileIdx <= 531) return animRange(tileIdx, 528, animationFrame, 4);
        if (tileIdx <= 535) return animRange(tileIdx, 532, animationFrame, 4);
        if (tileIdx <= 543) return animRange(tileIdx, 536, animationFrame, 8);
        if (tileIdx <= 553) return animRange(tileIdx, 546, animationFrame, 8);
        if (tileIdx <= 557) return animRange(tileIdx, 554, animationFrame, 4);
        if (tileIdx <= 561) return animRange(tileIdx, 558, animationFrame, 4);
        if (tileIdx <= 579) return animRange(tileIdx, 572, animationFrame, 8);
        if (tileIdx <= 583) return animRange(tileIdx, 580, animationFrame, 4);
        if (tileIdx <= 587) return animRange(tileIdx, 584, animationFrame, 4);
        if (tileIdx <= 595) return animRange(tileIdx, 588, animationFrame, 8);
        if (tileIdx <= 605) return animRange(tileIdx, 598, animationFrame, 8);
        if (tileIdx <= 609) return animRange(tileIdx, 606, animationFrame, 4);
        if (tileIdx <= 613) return animRange(tileIdx, 610, animationFrame, 4);
        if (tileIdx <= 627) return animRange(tileIdx, 624, animationFrame, 4);
        if (tileIdx <= 631) return animRange(tileIdx, 628, animationFrame, 4);
        if (tileIdx <= 635) return animRange(tileIdx, 632, animationFrame, 4);
        if (tileIdx <= 639) return animRange(tileIdx, 636, animationFrame, 4);
        if (tileIdx <= 653) return animRange(tileIdx, 650, animationFrame, 4);
        if (tileIdx <= 657) return animRange(tileIdx, 654, animationFrame, 4);
        if (tileIdx <= 661) return animRange(tileIdx, 658, animationFrame, 4);
        if (tileIdx <= 665) return animRange(tileIdx, 662, animationFrame, 4);
        if (tileIdx <= 673) return animRange(tileIdx, 666, animationFrame, 8);
        return tiles[0];
    }

    private Rect animRange(int tileIdx, int offset, int animationFrame, int length) {
        if (tileIdx < offset) return tiles[0];
        int f = tileIdx - offset;
        int idx = ((f + animationFrame) % length) + offset;
        return tiles[idx];
    }
}
