package com.ieb.smalltest.sprite;

import android.graphics.Rect;

/** Single animation for a sprite. Has set of:
 * source rect for the texture,
 * and frame time in ms. */
public class Animation {
    public static final int FOREVER = -1;
    public static final int ONCE = 1;

    private final Rect[] src; // rectangles relative to the sprite sheet texture.
    private final int[] time; // time that each frame should be shown for.
    private final int frameCount;

    private int loops;
    private int frameDur;
    private int frameIdx;

    /** Generate frames on a row of the texture, with a single frame time
     * @param frameTime duration of each frame (milliseconds)
     * @param loops number of loops before animation ends. If `Animation.FOREVER`, the animation never ends
     * @param y top of the texture row
     * @param h height of each animation tile
     * @param w width of each animation tile
     * */
    public Animation(int frameTime, int loops, int w, int h, int y, int[] offsets){
        this.loops = loops;
        frameDur = 0;
        frameIdx = 0;
        frameCount = offsets.length;
        src = new Rect[frameCount];
        time = new int[frameCount];
        for (int i = 0; i < frameCount; i++) {
            int offset = offsets[i];
            src[i] = new Rect(offset, y, offset + w, y + h);
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
}
