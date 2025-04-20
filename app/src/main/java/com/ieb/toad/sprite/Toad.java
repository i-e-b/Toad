package com.ieb.toad.sprite;

import com.ieb.toad.input.VirtualGamepad;
import com.ieb.toad.sprite.core.Animation;
import com.ieb.toad.sprite.core.Flip;
import com.ieb.toad.sprite.core.SpriteSheetManager;
import com.ieb.toad.world.constraints.CarryingObject;
import com.ieb.toad.world.constraints.OnLadder;
import com.ieb.toad.world.constraints.StandingOnCreep;
import com.ieb.toad.world.constraints.StandingOnGround;
import com.ieb.toad.world.core.Camera;
import com.ieb.toad.world.core.Collision;
import com.ieb.toad.world.core.Constraint;
import com.ieb.toad.world.core.SimulationManager;
import com.ieb.toad.world.core.Thing;
import com.ieb.toad.world.platforms.LadderPlatform;

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
    private final Animation carry_left;
    private final Animation carry_right;

    private int desireDirection = 1; // negative = left, positive = right.
    private long jumpTimeLeftMs;
    private long coyoteTimeLeftMs;
    private double lastFramePx, lastFramePy, animMs;
    private boolean grounded; // true when we are standing on something and can jump
    public boolean climbing; // true when we are climbing a ladder or vine
    private boolean canClimb; // true when could start climbing
    private boolean carrying; // true when carrying something

    // Note, if grounded && climbing, show grounded animation

    public final long JUMP_TIME_MS = 165;
    public final long COYOTE_TIME_MS = 250;

    /** Load Toad graphics */
    public Toad(final SpriteSheetManager spriteSheetManager) {
        run_left = new Animation(64, Animation.FOREVER, spriteSheetManager.toad, Flip.None, new int[]{9,10,11,10});
        run_right = new Animation(64, Animation.FOREVER, spriteSheetManager.toad, Flip.Horz, new int[]{9,10,11,10});
        carry_left = new Animation(64, Animation.FOREVER, spriteSheetManager.toad, Flip.None, new int[]{12,13,14,13});
        carry_right = new Animation(64, Animation.FOREVER, spriteSheetManager.toad, Flip.Horz, new int[]{12,13,14,13});
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
        grounded = false;
    }


    /** User control is done during AI think time */
    @Override
    public int think(SimulationManager level, int ms) {
        ax = ay = 0;
        animMs += ms;

        // apply control
        mapControls();
        applyControlsToPhysics(ms);
        applyControlsToWorld(level, ms);

        return KEEP;
    }

    /** Pick up, throw, etc */
    private void applyControlsToWorld(SimulationManager level, int ms) {
        handleActionButton(level);
    }

    private void handleActionButton(SimulationManager level) {
        if (btnAction && !actionLock){
            CarryingObject carry = (CarryingObject)getConstraint(CarryingObject.class);
            if (carry != null){
                // Break constraint, throw object
                actionLock = true;
                level.removeConstraint(carry);
                carry.carried.px += radius * desireDirection;
                carry.carried.vx = (desireDirection * 250) + (vx * 2.0);
                carry.carried.vy = Math.abs(vx) * -2.0;
                carrying = false;
            } else {
                StandingOnCreep over = (StandingOnCreep)getConstraint(StandingOnCreep.class);
                if (over != null) { // Pick up the creep
                    pull_right.reset(); pull_left.reset();
                    actionLock = true;
                    carrying = true;
                    level.removeConstraint(over);
                    level.addConstraint(new CarryingObject(over.bottom, this, radius * 3));
                }
            }
        }
    }

    /** Update move/jump/etc based on controls */
    private void applyControlsToPhysics(int ms) {
        if (btnRight) {
            addPlayerSpeed(50, 0);
            desireDirection = 1;
        } else if (btnLeft) {
            addPlayerSpeed(-50, 0);
            desireDirection = -1;
        } else {
            reducePlayerSpeed();
        }

        if (canClimb){
            if (btnDown){
                climbing = true;
                vy = 250;
            } else if (btnUp){
                grounded = false;
                climbing = true;
                vy = -200;
            } else if (climbing) {
                vy = 0;
            }
            gravity = climbing ? 0.0 : 1.0; // don't fall while climbing
        } else {
            climbing = false;
            gravity = 1.0;
        }

        if (!grounded && coyoteTimeLeftMs > 0){
            coyoteTimeLeftMs -= ms;
        }
        if (grounded) coyoteTimeLeftMs = COYOTE_TIME_MS;

        if (btnJump){
            if ((grounded || coyoteTimeLeftMs > 0) && !jumpUsed) {
                jumpUsed = true;
                jumpTimeLeftMs = JUMP_TIME_MS;
                coyoteTimeLeftMs = 0;
            }

            if (jumpTimeLeftMs > 0){
                jumpTimeLeftMs -= ms;
                vy = -900;
            }
        } else {
            jumpTimeLeftMs = 0;
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
        if (!btnAction) actionLock = false;
    }

    public void addPlayerSpeed(double dx, int dy) {
        vy += dy;
        vx += dx;
    }
    public void reducePlayerSpeed(){
        vx /= 1.2;
    }

    private boolean btnAction, btnUp, btnJump, btnDown, btnRight, btnLeft,
            jumpUsed, actionLock;

    @Override
    public void draw(@NotNull Camera camera, int frameMs) {
        double dx = Math.abs(px - lastFramePx);
        double dy = Math.abs(py - lastFramePy);
        lastFramePx = px;
        lastFramePy = py;

        Animation a = pickAnimation(dx, dy, animMs);

        camera.drawSprite(a, px, py, radius);
        animMs = 0;
    }

    private Animation pickAnimation(double dx, double dy, double animMs) {
        if (climbing){
            return climb.advance(dy);
        }
        if (carrying){
            pull_right.advance(animMs);
            pull_left.advance(animMs);
            Animation p = desireDirection > 0 ? pull_right : pull_left;
            if (!p.isEnded()) return p;
            Animation a = desireDirection > 0 ? carry_right : carry_left;
            return a.advance(dx); // animate based on movement
        }
        if (grounded) {
            if (btnDown) return crouch_centre.advance(animMs);
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

        if (hasFlag(other.type, Collision.CREEP)){
            // we hit a creep. Might want to stand on it
            if (!grounded && this.canLandOnTop(other)){
                level.addConstraint(new StandingOnCreep(this, other));
            }
        } else if (hasFlag(other.type, Collision.WALL)) {
            // we might be standing on a floor on over a ladder/vine
            Class<? extends Thing> wallType = other.getClass();

            if (wallType == LadderPlatform.class) {
                if (!canClimb) level.addConstraint(new OnLadder(this, other));
            }
            else if (!grounded && this.canLandOnTop(other)) {
                level.addConstraint(new StandingOnGround(this, other));
            }
        }
    }

    private static boolean hasFlag(int type, int flag) {
        return (type & flag) == flag;
    }

    /** [Optional Override]
     * Perform any actions or checks when a constrain is first added
     */
    @Override
    protected void constrainAdded(Constraint c) {
        updateConstraintState();
    }

    /** [Optional Override]
     * Perform any actions or checks when a constrain is first removed
     */
    @Override
    protected void constrainRemoved(Constraint c) {
        updateConstraintState();
    }

    private void updateConstraintState(){
        // Reset first
        grounded = false;
        canClimb = false;
        carrying = false;

        // Update based on current constraints
        for (Constraint c : linkedConstraints()) {
            Class<? extends Constraint> cType = c.getClass();
            if (cType == StandingOnCreep.class || cType == StandingOnGround.class){
                grounded = true;
            } else if (cType == OnLadder.class){
                canClimb = true;
            } else if (cType == CarryingObject.class) {
                carrying = true;
            }
        }
    }
}
