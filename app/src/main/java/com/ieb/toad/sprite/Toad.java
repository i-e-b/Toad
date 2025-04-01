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
    private final Animation fall_left;
    private final Animation fall_right;
    private final Animation stand_left;
    private final Animation stand_right;
    private final Animation crouch_centre;
    private final Animation climb;
    private final Animation pull_left;
    private final Animation pull_right;

    private int desireDirection = 1; // negative = left, positive = right.
    private Thing climbing = null; // vine/ladder
    private long jumpTimeLeftMs;
    private double lastFramePx, lastFramePy, animMs;

    /** @noinspection FieldCanBeLocal*/
    private final long JUMP_TIME_MS = 165;

    /** Load Toad graphics */
    public Toad(final SpriteSheetManager spriteSheetManager) {
        run_left = new Animation(64, Animation.FOREVER, spriteSheetManager.toad, Flip.None, new int[]{9,10,11,10});
        run_right = new Animation(64, Animation.FOREVER, spriteSheetManager.toad, Flip.Horz, new int[]{9,10,11,10});
        fall_left = new Animation(64, Animation.FOREVER, spriteSheetManager.toad, Flip.None, new int[]{16});
        fall_right = new Animation(64, Animation.FOREVER, spriteSheetManager.toad, Flip.Horz, new int[]{16});
        stand_left = new Animation(64, Animation.FOREVER, spriteSheetManager.toad, Flip.None, new int[]{9});
        stand_right = new Animation(64, Animation.FOREVER, spriteSheetManager.toad, Flip.Horz, new int[]{9});
        crouch_centre = new Animation(64, Animation.FOREVER, spriteSheetManager.toad, Flip.None, new int[]{17});

        climb = new Animation(64, Animation.FLIP_REPEAT, spriteSheetManager.toad, Flip.Horz, new int[]{15});

        pull_left = new Animation(200, Animation.ONCE, spriteSheetManager.toad, Flip.None, new int[]{19,20,12});
        pull_right = new Animation(200, Animation.ONCE, spriteSheetManager.toad, Flip.Horz, new int[]{19,20,12});

        type = Collision.PLAYER;
        radius = 29;
        gravity = 1.0; // fully affected by gravity
    }


    /** User control is done during AI think time */
    @Override
    public int think(SimulationManager level, int ms) {
        ax = ay = 0;
        animMs += ms;
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

        if (btnJump){
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
        btnUp = VirtualGamepad.isUp();
        btnDown = VirtualGamepad.isDown();
        btnRight = VirtualGamepad.isRight();
        btnJump = VirtualGamepad.isJump();
        btnLeft = VirtualGamepad.isLeft();

        boolean prevAct = btnAction;
        btnAction = VirtualGamepad.isAction();
        if (btnAction && !prevAct){
            pull_left.reset();
            pull_right.reset();
        }
    }

    public void addPlayerSpeed(double dx, int dy) {
        vy += dy;
        vx += dx;
    }

    private boolean btnAction, btnUp, btnJump, btnDown, btnRight, btnLeft, jumpUsed;

    @Override
    public void draw(@NotNull Camera camera) {
        double dx = Math.abs(px - lastFramePx);
        double dy = Math.abs(py - lastFramePy);
        lastFramePx = px;
        lastFramePy = py;

        Animation a = pickAnimation(dx, dy, animMs);

        camera.drawSprite(a, px, py, radius);
        animMs = 0;
    }

    private Animation pickAnimation(double dx, double dy, double animMs) {
        if (jumpTimeLeftMs >= JUMP_TIME_MS - 50){
            if (btnAction) return desireDirection > 0 ? pull_right.advance(animMs) : pull_left.advance(animMs);
            if (btnDown) return crouch_centre.advance(animMs);
            if (btnUp) return climb.advance(dy);
            if (Math.abs(vx) < 1) return desireDirection > 0 ? stand_right : stand_left;

            Animation a = desireDirection > 0 ? run_right : run_left;
            return a.advance(dx); // animate based on movement
        }

        return desireDirection > 0 ? fall_right : fall_left;
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
                //vx = other.vx; // match speed for easy landing
                level.addConstraint(new VerticalSpring(this, other, 16));
                //level.addConstraint(new FixedLength(this, other, 0));
            }
        } else if (other.type == Collision.WALL) { // we might be standing on a floor
            // restore jump?
            if (this.canLandOnTop(other)) jumpTimeLeftMs = JUMP_TIME_MS;
        }
    }
}
