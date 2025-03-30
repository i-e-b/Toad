package com.ieb.toad.world.loader;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;

import com.ieb.toad.sprite.core.SpriteSheetManager;

public class LayerChunk {
    private final int[] tiles;
    private final int width;
    private final int height;
    private final SpriteSheetManager sprites;

    public final int left;
    public final int top;
    public final String key;

    private Bitmap cache;

    /**
     * Create a new layer chunk
     * @param spriteMgr source for tile graphics
     * @param iw width of chunk, in tile count
     * @param ih height of chunk, in tile count
     * @param x left edge of chunk in world co-ords
     * @param y top edge of chunk in world co-ords
     */
    public LayerChunk(SpriteSheetManager spriteMgr, int iw, int ih, int x, int y) {
        sprites = spriteMgr;
        tiles = new int[iw*ih];
        width = iw;
        height = ih;
        left = x;
        top = y;
        key = x+":"+y;
    }

    /** Return a bitmap for this layer chunk.
     * This will be at a single pixel scale.
     */
    public Bitmap getBitmap() {
        if (cache != null) return cache;

        Bitmap.Config bitmapConfig = Bitmap.Config.ARGB_8888;
        int tilePx = sprites.tiles.pixelSize;
        int pixelWidth = width * tilePx;
        int pixelHeight = height * tilePx;

        cache = Bitmap.createBitmap(pixelWidth, pixelHeight, bitmapConfig);
        Canvas canvas = new Canvas(cache);
        Rect dst = new Rect();

        for (int iy = 0; iy < height; iy++){
            int span = iy * width;
            int py = iy * tilePx;
            for (int ix = 0; ix < width; ix++){
                int tileIdx = tiles[span + ix];
                if (tileIdx < 0) continue;

                int px = ix * tilePx;
                Rect src = sprites.tiles.tiles[tileIdx];
                dst.set(px,py,px+tilePx, py+tilePx);
                canvas.drawBitmap(sprites.tiles.bitmap, src, dst, null);
            }
        }

        return cache;
    }

    public void set(int idx, int tile) {
        if (idx >= tiles.length) return;
        tiles[idx] = tile;
    }
}
