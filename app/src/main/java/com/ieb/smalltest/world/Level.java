package com.ieb.smalltest.world;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.ieb.smalltest.Main;
import com.ieb.smalltest.sprite.Toad;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * A level with walls, creeps, and a player
 */
public class Level {

    /**
     * Set of things for the level
     */
    private final Thing[] things; // TODO: better structure for larger levels
    private final Simulator simulator;

    public Level(Main context) throws IOException {
        simulator = new Simulator();

        // TODO: text-to-level, like in Minikoban.
        things = new Thing[9];

        things[0] = new Toad(context);
        things[0].p0x = 100;
        things[0].p0y = 600;

        things[1] = new Platform(0, 500, 1000, 16);
        things[2] = new ConveyorPlatform(450, 800, 350, 16, -6.0);
        things[3] = new Platform(0, 500, 16, 800);
        things[4] = new Platform(0, 1300, 2000, 16);
        things[5] = new Platform(1000, 500, 16, 800);
        things[6] = new OneWayPlatform(0, 800, 128, 16);
        things[7] = new OneWayPlatform(0, 1000, 128, 16);
        things[8] = new LifterPlatform(890, 1000, 32, 300, 700.0);
    }

    public void Draw(@NotNull Canvas canvas, Paint paint, int width, int height, int frameMs) {
        // TODO: pass drawing through a 'Camera' to do offsets/scrolling
        for (Thing thing : things) {
            thing.draw(canvas, paint);
        }
    }

    private long jumpTimeLeftMs;

    /**
     * Run the level for up to `ms` milliseconds.
     * Returns number of milliseconds run.
     */
    public long stepMillis(long ms) {
        // apply control
        if (btnRight) addPlayerSpeed(50, 0);
        if (btnLeft) addPlayerSpeed(-50, 0);

        if (btnUp){
            if (jumpTimeLeftMs > 0){
                jumpTimeLeftMs -= ms;
                things[0].v0y = -900;
            }
        } else {
            jumpTimeLeftMs = 300;
        }

        // apply physics
        double time = (double) ms;
        double nextTime = simulator.solve(time, things);

        // reset if out-of-bounds
        if (things[0].p0y > 2000) {
            things[0].p0x = 100;
            things[0].p0y = 600;
        }

        // copy button states
        prevBtnAction= btnAction;
        prevBtnUp = btnUp;
        prevBtnDown = btnDown;
        prevBtnRight = btnRight;
        prevBtnLeft = btnLeft;

        // return simulated time
        return (long) (nextTime);
    }

    public void addPlayerSpeed(double dx, int dy) {
        things[0].v0y += dy;
        things[0].v0x += dx;
    }

    private boolean btnAction, btnUp, btnDown, btnRight, btnLeft;
    private boolean prevBtnAction, prevBtnUp, prevBtnDown, prevBtnRight, prevBtnLeft;

    public boolean isInputLeftHeld(){
        return btnLeft && prevBtnLeft;
    }
    public boolean isInputRightHeld(){
        return btnRight && prevBtnRight;
    }

    /**
     * Player 'action' button changed
     */
    public void input_action(boolean isDown) {
        btnAction = isDown;
    }

    /**
     * Player 'up' button changed
     */
    public void input_up(boolean isDown) {
        btnUp = isDown;
    }

    /**
     * Player 'down' button changed
     */
    public void input_down(boolean isDown) {
        btnDown = isDown;
    }

    /**
     * Player 'right' button changed
     */
    public void input_right(boolean isDown) {
        btnRight = isDown;
    }

    /**
     * Player 'left' button changed
     */
    public void input_left(boolean isDown) {
        btnLeft = isDown;
    }
}
