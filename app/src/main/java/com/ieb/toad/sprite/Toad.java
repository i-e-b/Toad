package com.ieb.toad.sprite;

import com.ieb.toad.input.VirtualGamepad;
import com.ieb.toad.sprite.core.Animation;
import com.ieb.toad.sprite.core.Flip;
import com.ieb.toad.sprite.core.SpriteSheetManager;
import com.ieb.toad.world.constraints.FixedLength;
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
    public void think(SimulationManager level, int ms) {
        // apply control
        mapControls();
        applyControlsToPhysics(ms);
    }

    private void applyControlsToPhysics(int ms) {
        if (btnRight) {
            addPlayerSpeed(50, 0);
            desireDirection = 1;
        }
        if (btnLeft) {
            addPlayerSpeed(-50, 0);
            desireDirection = -1;
        }

        if (btnUp){
            if (jumpTimeLeftMs > 0){
                jumpTimeLeftMs -= ms;
                vy = -900;
            }
        } else {
            jumpTimeLeftMs = 300;
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

    private boolean btnAction, btnUp, btnDown, btnRight, btnLeft;

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
            /*
            // quick and dirty: apply creep's speed:
            if (this.canLandOnTop(other)){
                px = other.px;
                vy = other.vy;
            }*/
            // quick and dirty 2: add a constraint if none already
            if (!anyConstraints() && this.canLandOnTop(other)){
                double length = this.radius+other.radius;
                level.addConstraint(new FixedLength(this, other, length));
            }
        } else if (other.type == Collision.WALL) { // we might be standing on a floor
            // restore jump?
        }
    }
}
