package com.ieb.toad.sprite;

import com.ieb.toad.input.VirtualGamepad;
import com.ieb.toad.sprite.core.Animation;
import com.ieb.toad.sprite.core.Flip;
import com.ieb.toad.sprite.core.SpriteSheetManager;
import com.ieb.toad.world.constraints.VerticalSpring;
import com.ieb.toad.world.core.Camera;
import com.ieb.toad.world.core.Collision;
import com.ieb.toad.world.core.SimulationManager;
import com.ieb.toad.world.core.Thing;

import org.jetbrains.annotations.NotNull;

public class Toad extends Thing {

    private final Animation run_left;
    private final Animation run_right;

    private int desireDirection = 1; // negative = left, positive = right.
    private long jumpTimeLeftMs;
    private double lastFramePx;

    /** @noinspection FieldCanBeLocal*/
    private final long JUMP_TIME_MS = 165;

    /** Load Toad graphics */
    public Toad(final SpriteSheetManager spriteSheetManager) {
        run_left = new Animation(64, Animation.FOREVER, spriteSheetManager.toad, Flip.None, new int[]{9,10,11,10});
        run_right = new Animation(64, Animation.FOREVER, spriteSheetManager.toad, Flip.Horz, new int[]{9,10,11,10});

        type = Collision.PLAYER;
        radius = 30;
        gravity = 1.0; // fully affected by gravity
    }


    /** User control is done during AI think time */
    @Override
    public int think(SimulationManager level, int ms) {
        ax = ay = 0;
        // apply control
        mapControls();
        applyControlsToPhysics(level, ms);
        return KEEP;
    }

    private void applyControlsToPhysics(SimulationManager level, int ms) {
        if (btnRight) {
            addPlayerSpeed(50, 0);
            desireDirection = 1;
        }
        if (btnLeft) {
            addPlayerSpeed(-50, 0);
            desireDirection = -1;
        }

        if (btnUp){
            if (anyConstraints()){ // assume it's a standing-constraint for now. TODO: be more specific
                level.removeConstraint(this.constraints.get(0));
                jumpTimeLeftMs = JUMP_TIME_MS;
            }
            if (jumpTimeLeftMs > 0 && !jumpUsed){
                jumpTimeLeftMs -= ms;
                vy = -900;
            } else {
                jumpUsed = true;
            }
        } else {
            if (jumpTimeLeftMs > 0) jumpTimeLeftMs -= 30; // Coyote time. TODO: base on ground constraint?
            jumpUsed = false;
        }
    }

    /** Map current VirtualGamepad state to level controls */
    private void mapControls() {
        btnDown = VirtualGamepad.isDown();
        btnRight = VirtualGamepad.isRight();
        btnUp = VirtualGamepad.isUp();
        btnLeft = VirtualGamepad.isLeft();
        btnAction = VirtualGamepad.isAction();
    }

    public void addPlayerSpeed(double dx, int dy) {
        vy += dy;
        vx += dx;
    }

    private boolean btnAction, btnUp, btnDown, btnRight, btnLeft, jumpUsed;

    @Override
    public void draw(@NotNull Camera camera) {
        double dx = Math.abs(px - lastFramePx);
        lastFramePx = px;

        Animation a = desireDirection > 0 ? run_right : run_left;
        a.advance(dx); // animate based on movement

        camera.drawSprite(a, px, py, radius);
    }

    /** Handle collisions with things.
     * @param other thing we might have touched.
     * @param impacted true if there was an impact
     */
    @Override
    public void impactResolve(SimulationManager level, Thing other, boolean impacted) {
        if (!impacted) return;

        if (other.type == Collision.CREEP){ // we hit a creep. Might want to stand on it
            // quick and dirty: add a constraint if none already
            if (!anyConstraints() && this.canLandOnTop(other)){
                vx = other.vx; // match speed for easy landing
                level.addConstraint(new VerticalSpring(this, other, 16));
            }
        } else if (other.type == Collision.WALL) { // we might be standing on a floor
            // restore jump?
            if (this.canLandOnTop(other)) jumpTimeLeftMs = JUMP_TIME_MS;
        }
    }
}
