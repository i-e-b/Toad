package com.ieb.toad.sprite.core;

import android.graphics.Bitmap;
import android.graphics.Rect;

/** Single animation for a sprite. Has set of:
 * source rect for the texture,
 * and frame time in ms. */
public class Animation {
    /** cycle animation forever */
    public static final int FOREVER = -1;
    /** cycle forever, flipping horizontal on each cycle */
    public static final int FLIP_REPEAT = -2;
    /** animate once then stop */
    public static final int ONCE = 1;

    /** sprite scale. Defaults to 4x */
    public int scale = 4;

    private int flip; // bitmap to use. See sprite.core.Flip

    private final Rect[] src; // rectangles relative to the sprite sheet texture.
    private final Bitmap[] bitmap; // bitmaps in flip directions
    private final int[] time; // time that each frame should be shown for.
    private final int frameCount;

    private final int originalLoops;
    private int loops;
    private double frameDur;
    private int frameIdx;

    /** Generate frames on a row of the texture, with a single frame time
     * @param frameTime duration of each frame (milliseconds)
     * @param loops number of loops before animation ends. If `Animation.FOREVER`, the animation never ends
     * @param sheet source image and tiles for the animation
     * @param flip image flip variant 0..3
     * @param tileIndexes indexes in tileSheet to use for this animation. Indexes can be repeated.
     * */
    public Animation(int frameTime, int loops, SpriteSheet sheet, int flip, int[] tileIndexes){
        originalLoops = loops;
        this.loops = loops;
        this.flip = flip;
        frameDur = 0;
        frameIdx = 0;
        frameCount = tileIndexes.length;
        src = new Rect[frameCount];
        time = new int[frameCount];
        for (int i = 0; i < frameCount; i++) {
            int offset = tileIndexes[i];
            src[i] = sheet.tiles[offset];
            time[i] = frameTime;
        }
        bitmap = new Bitmap[sheet.bitmap.length];
        System.arraycopy(sheet.bitmap, 0, bitmap, 0, sheet.bitmap.length);
    }

    /** Generate frames on a row of the texture, with a single frame time
     * @param frameTime duration of each frame (milliseconds)
     * @param loops number of loops before animation ends. If `Animation.FOREVER`, the animation never ends
     * @param sheet source image and tiles for the animation
     * @param tileIndexes indexes in tileSheet to use for this animation. Indexes can be repeated.
     * */
    public Animation(int frameTime, int loops, TileSheet sheet, int[] tileIndexes){
        originalLoops = loops;
        this.loops = loops;
        this.flip = 0;
        frameDur = 0;
        frameIdx = 0;
        frameCount = tileIndexes.length;
        src = new Rect[frameCount];
        time = new int[frameCount];
        for (int i = 0; i < frameCount; i++) {
            int offset = tileIndexes[i];
            src[i] = sheet.tiles[offset];
            time[i] = frameTime;
        }
        bitmap = new Bitmap[1];
        bitmap[0] = sheet.bitmap;
    }

    public Rect rect() {
        return src[frameIdx];
    }

    public boolean isEnded(){
        return loops < 1;
    }

    /** reset loop count for animation */
    public void reset() {
        if (originalLoops <= 0) return;
        loops = originalLoops;
        frameIdx = 0;
        frameDur = 0;
    }

    public Animation advance(double ms) {
        frameDur += ms;
        while (frameDur > time[frameIdx]) {
            frameDur -= time[frameIdx];
            frameIdx++;

            if (frameIdx < frameCount) continue;

            // cycle ended
            if (loops > 1) {
                loops--;
                frameIdx = 0;
            } else if (loops == FOREVER){
                frameIdx = 0;
            } else if (loops == FLIP_REPEAT){
                flip = flip == Flip.Horz ? Flip.None : Flip.Horz;
                frameIdx = 0;
            } else {
                loops = 0;
                frameIdx = frameCount - 1;
                break;
            }
        }
        return this;
    }

    public Bitmap bitmap() {
        return bitmap[flip];
    }

}
