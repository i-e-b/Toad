package com.ieb.toad.sprite;

import com.ieb.toad.sprite.core.Animation;
import com.ieb.toad.sprite.core.Flip;
import com.ieb.toad.sprite.core.SpriteSheetManager;
import com.ieb.toad.world.constraints.CarryingObject;
import com.ieb.toad.world.constraints.StandingOnCreep;
import com.ieb.toad.world.core.Camera;
import com.ieb.toad.world.core.Collision;
import com.ieb.toad.world.core.SimulationManager;
import com.ieb.toad.world.core.Thing;

import org.jetbrains.annotations.NotNull;

public class Shy extends Thing {
    private final Animation left;
    private final Animation right;
    private final Animation flipLeft;
    private final Animation flipRight;

    private double lastFramePx;
    private boolean carried;

    private int desireDirection = -1; // negative = left, positive = right.

    public final int SPEED = 120, ACCEL = 500;
    private double dpx; // px value to restore after collision

    /** Load Toad graphics */
    public Shy(final SpriteSheetManager sprites) {
        left = new Animation(160, Animation.FOREVER, sprites.dude, Flip.None, new int[]{0,1});
        right = new Animation(160, Animation.FOREVER, sprites.dude, Flip.Horz, new int[]{0,1});
        flipLeft = new Animation(160, Animation.FOREVER, sprites.dude, Flip.Vert, new int[]{0,1});
        flipRight = new Animation(160, Animation.FOREVER, sprites.dude, Flip.Horz + Flip.Vert, new int[]{0,1});

        carried = false;
        type = Collision.CREEP;
        radius = 30;
        mass = 0.8;
        gravity = 1.0; // fully affected by gravity
    }

    @Override
    public int think(SimulationManager level, int ms) {
        ax = ay = 0;

        CarryingObject carry = (CarryingObject)getConstraint(CarryingObject.class);
        if (carry != null){
            type = Collision.CREEP | Collision.PASS_THROUGH;
            gravity = 0.0;
            carried = true;
            carryThink(carry);
        } else {
            type = Collision.CREEP;
            carried = false;
            walkingThink(level);
        }

        return KEEP;
    }

    private void carryThink(CarryingObject carry) {
        double dx = carry.holder.vx;
        if (dx > 0) desireDirection = 1;
        if (dx < 0) desireDirection = -1;
    }

    private void walkingThink(SimulationManager level) {
        // Switch direction if facing wall or pit
        var frontSense = level.hitTest(px + ((radius + 4) * desireDirection), py);

        if (Collision.hasPlayer(frontSense)){
            level.damagePlayer();
        }

        if (Collision.hasWall(frontSense)) {
            desireDirection = -desireDirection; // turn on wall
        } else {
            var pitSense = level.hitTest(px + ((radius * 2) * desireDirection), py+radius+5);
            var groundSense = level.hitTest(px, py+radius+5);
            if (Collision.hasWall(groundSense) && !Collision.hasWall(pitSense))
                desireDirection = -desireDirection; // turn on pit, only if on ground
        }

        // go slow if we've being stood on
        double speed = hasConstraint(StandingOnCreep.class) ? SPEED / 2.0 : SPEED;

        if (desireDirection < 0){ // left
            if (vx > -speed) ax = -ACCEL;
        } else { // right
            if (vx < speed) ax = ACCEL;
        }
    }

    @Override
    public void draw(@NotNull Camera camera, int frameMs) {
        double dx = Math.abs(px - lastFramePx);
        lastFramePx = px;

        Animation anim;
        if (carried){
            anim = desireDirection > 0 ? flipRight : flipLeft;
            anim.advance(frameMs); // animate based on time
        } else {
            anim = desireDirection > 0 ? right : left;
            anim.advance(dx*10); // animate based on movement
        }

        camera.drawSprite(anim, px, py, radius);
    }

    @Override
    public void preImpactTest(Thing other) {
        dpx = 0;
        if (other.type != Collision.PLAYER) return; // normal collision for anything but a player
        if (!other.canLandOnTop(this)) return;

        // Player is above us. adjust px to make it easy to stand on top
        dpx = px;
        px = clamp(other.px, px-radius, px+radius);
        dpx -= px;
    }

    @Override
    public void postImpactTest() {
        px += dpx;
    }
}
