package com.ieb.toad.world.loader;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
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
    private int animationTime, animationFrame;
    private boolean dirty;

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
        dirty = true;
    }

    /** Return a bitmap for this layer chunk.
     * This will be at a single pixel scale.
     */
    public Bitmap getBitmap() {
        if (cache != null && !dirty) return cache;

        int tilePx = sprites.tiles.pixelSize;

        if (cache == null){
            int pixelWidth = width * tilePx;
            int pixelHeight = height * tilePx;
            Bitmap.Config bitmapConfig = Bitmap.Config.ARGB_8888;

            cache = Bitmap.createBitmap(pixelWidth, pixelHeight, bitmapConfig);
        }
        Canvas canvas = new Canvas(cache);
        canvas.drawColor(0, PorterDuff.Mode.CLEAR);
        Rect dst = new Rect();

        for (int iy = 0; iy < height; iy++){
            int span = iy * width;
            int py = iy * tilePx;
            for (int ix = 0; ix < width; ix++){
                int tileIdx = tiles[span + ix];
                if (tileIdx < 0) continue;

                int px = ix * tilePx;
                Rect src = sprites.tiles.getTile(tileIdx, animationFrame);
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

    /** step time forward for animations */
    public void advanceTime(int frameMs) {
        animationTime += frameMs;
        if (animationTime > 200){
            animationFrame++;
            animationTime -= 200;
            dirty = true;
        }
    }
}
