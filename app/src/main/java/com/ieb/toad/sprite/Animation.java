package com.ieb.toad.sprite;

import android.graphics.Bitmap;
import android.graphics.Rect;

/** Single animation for a sprite. Has set of:
 * source rect for the texture,
 * and frame time in ms. */
public class Animation {
    public static final int FOREVER = -1;
    public static final int ONCE = 1;

    private final SpriteSheet sheet; // reference to sprite sheet image
    private final Rect[] src; // rectangles relative to the sprite sheet texture.
    private final int[] time; // time that each frame should be shown for.
    private final int frameCount;

    private int loops;
    private int frameDur;
    private int frameIdx;

    /** Generate frames on a row of the texture, with a single frame time
     * @param frameTime duration of each frame (milliseconds)
     * @param loops number of loops before animation ends. If `Animation.FOREVER`, the animation never ends
     * @param spriteSheet source image and tiles for the animation
     * @param tileIndexes indexes in tileSheet to use for this animation. Indexes can be repeated.
     * */
    public Animation(int frameTime, int loops, SpriteSheet spriteSheet, int[] tileIndexes){
        this.loops = loops;
        sheet = spriteSheet;
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
    }

    public Rect rect() {
        return src[frameIdx];
    }

    public boolean isEnded(){
        return loops < 1;
    }

    public void advance(long ms) {
        frameDur += (int) ms;
        while (frameDur > time[frameIdx]) {
            frameDur -= time[frameIdx];
            frameIdx++;
            if (frameIdx >= frameCount) {
                if (loops != 0) {
                    loops--;
                    if (loops < -1) loops = -1;
                    frameIdx = 0;
                } else {
                    frameIdx = frameCount - 1;
                }
            }
        }
    }

    public Bitmap bitmap() {
        return sheet.bitmap;
    }
}
