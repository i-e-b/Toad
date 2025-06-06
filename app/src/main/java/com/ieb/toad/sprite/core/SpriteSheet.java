package com.ieb.toad.sprite.core;

import android.graphics.Bitmap;
import android.graphics.Rect;

import java.util.List;

/** Holds reference to a bitmap, plus an ordered set of rectangles within it */
public class SpriteSheet {

    public final Bitmap[] bitmap;

    public final Rect[] tiles;

    public SpriteSheet(Bitmap src, List<Rect> tileList){
        tiles = tileList.toArray(new Rect[0]);

        bitmap = new Bitmap[4];
        bitmap[Flip.None] = src;

        Bitmap.Config bitmapConfig = Bitmap.Config.ARGB_8888;

        bitmap[Flip.Horz] = src.copy(bitmapConfig, true);
        flipTilesHorz(bitmap[Flip.Horz]);

        bitmap[Flip.Vert] = src.copy(bitmapConfig, true);
        flipTilesVert(bitmap[Flip.Vert]);

        bitmap[Flip.Both] = bitmap[Flip.Vert].copy(bitmapConfig, true);
        flipTilesHorz(bitmap[Flip.Both]);
    }

    private void flipTilesHorz(Bitmap bmp) { // TODO: optimise this after it's fully working
        for (Rect tile : tiles) { // each tile
            for (int y = tile.top; y <= tile.bottom; y++){ // each row
                int xr = tile.right-1;
                for (int x = tile.left; x < xr; x++){ // mirror left to right
                    int lc = bmp.getPixel(x,y);
                    int rc = bmp.getPixel(xr,y);
                    bmp.setPixel(x,y, rc);
                    bmp.setPixel(xr,y, lc);
                    xr--;
                }
            }
        }
    }

    private void flipTilesVert(Bitmap bmp) { // TODO: optimise this after it's fully working
        for (Rect tile : tiles) { // each tile
            for (int x = tile.left; x <= tile.right; x++){ // each column
                int yb = tile.bottom-1;
                for (int y = tile.top; y < yb; y++){ // mirror top to bottom
                    int tc = bmp.getPixel(x,y);
                    int bc = bmp.getPixel(x,yb);
                    bmp.setPixel(x,y, bc);
                    bmp.setPixel(x,yb, tc);
                    yb--;
                }
            }
        }
    }
}
