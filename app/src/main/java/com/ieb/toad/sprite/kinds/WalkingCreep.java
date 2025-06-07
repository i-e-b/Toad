package com.ieb.toad.sprite.kinds;

import android.util.Log;

import com.ieb.toad.sprite.Bullet;
import com.ieb.toad.sprite.core.SpriteSheetManager;
import com.ieb.toad.world.constraints.CarryingObject;
import com.ieb.toad.world.constraints.StandingOnCreep;
import com.ieb.toad.world.core.Collision;
import com.ieb.toad.world.core.SimulationManager;
import com.ieb.toad.world.core.Thing;

public abstract class WalkingCreep extends Thing {
    public double normalRadius;
    final SpriteSheetManager sprites;

    double throwTimer,      // timer to turn off collision for a short time after being thrown
            recoverTimer,   // timer for getting up if we are thrown bt not carried
            fireTimer,      // periodic bullet timer
            turnTimer;      // timer to stop us vibrating on the spot
    public boolean carried, thrown;

    public int desireDirection = -1; // negative = left, positive = right.

    public final int SPEED = 120, ACCEL = 500, FIRE_RATE = 5000;
    double dpx; // px value to restore after collision
    public boolean grounded; // recently on the ground
    public boolean canShoot;
    public boolean onLedge; // don't walk, face out and wait

    public WalkingCreep(final SpriteSheetManager sprites) {
        this.sprites = sprites;
        fireTimer = FIRE_RATE;
        carried = thrown = false;
        type = Collision.CREEP;
        normalRadius = radius = 30;
        mass = 0.8;
        elasticity = 0.5;
        gravity = 1.0; // fully affected by gravity
        canShoot = false;
        onLedge = false;
        turnTimer = 0.0;
    }

    @Override
    public int think(SimulationManager level, int ms) {
        ax = ay = 0;
        if (throwTimer > 0.0) throwTimer -= ms;
        if (turnTimer > 0.0) turnTimer -=ms;

        CarryingObject carry = (CarryingObject)getConstraint(CarryingObject.class);
        if (carry != null) { // being carried
            type = Collision.CREEP | Collision.PASS_THROUGH;
            gravity = 0.0;
            carried = true;
            carryThink(carry);
        } else if (carried || thrown) { // thrown
            if (carried) {
                recoverTimer = 5000.0; // time after stationary that creep flips back over
                throwTimer = 16.0; // time until collision is restored
            }
            gravity = 1.0;
            drag = grounded ? 0.05 : 0.0;
            elasticity = 0.9;
            carried = false;
            thrown = true;

            // flip if on ground for a long time
            if (grounded && recoverTimer > 0.0){
                recoverTimer -= ms;
            }
            if (recoverTimer < 1.0){
                thrown = false;
            }

            type = Collision.CREEP;
        } else { // walking around
            drag = DEFAULT_DRAG;
            elasticity = 0.5;
            type = Collision.CREEP;
            walkingThink(level, ms);
        }

        grounded = false; // will be reset if still on ground
        return KEEP;
    }

    private void carryThink(CarryingObject carry) {
        double dx = carry.holder.vx;
        if (dx > 0) desireDirection = 1;
        if (dx < 0) desireDirection = -1;
    }

    private void walkingThink(SimulationManager level, int ms) {
        if (desireDirection == 0) desireDirection = -1;

        // Switch direction if facing wall or pit
        var frontSense = level.hitTest(px + ((radius + 4) * desireDirection), py);

        if (Collision.hasPlayer(frontSense)){
            level.damagePlayer();
        }

        if (turnTimer < 1500) { // don't spin on the spot
            turningLogic(level, frontSense);
        }

        // Fire a bullet periodically
        fireTimer -= ms;
        if (fireTimer <= 0.0) {
            if (canShoot) fireBullet(level);
            fireTimer += FIRE_RATE;
        }

        if (onLedge) {
            // we want to stand still
            vx = 0.0;
        } else { // we want to walk
            // go slow if we've being stood on
            double speed = hasConstraint(StandingOnCreep.class) ? SPEED / 2.0 : SPEED;
            if (desireDirection < 0){ // left
                if (vx > -speed) ax = -ACCEL;
            } else { // right
                if (vx < speed) ax = ACCEL;
            }
        }
    }

    private void turningLogic(SimulationManager level, int frontSense) {
        if (Collision.hasWall(frontSense) || Collision.hasCreep(frontSense)) {
            desireDirection = -desireDirection; // turn on wall
            turnTimer += 500;
        } else {
            var pitSense = level.hitTest(px + ((radius * 2) * desireDirection), py+radius+5);

            if (!Collision.hasWall(pitSense)){
                // if there is a pit ahead...

                var behindSense = level.hitTest(px + ((radius + 4) * (-desireDirection)), py);
                if (Collision.hasWall(behindSense)) {   // if there is a wall directly behind us
                    onLedge = true;                     // stop here and face out
                } else {
                    var groundSense = level.hitTest(px, py+radius+5);
                    if (Collision.hasWall(groundSense)) {   // if we are on the ground,
                        desireDirection = -desireDirection; // turn away from the pit
                        turnTimer += 500;
                    }
                }
            }
        }
    }

    private void fireBullet(SimulationManager level) {
        Bullet b = new Bullet(sprites, desireDirection, px + (desireDirection*radius/2), py);
        level.addThing(b);
    }

    @Override
    public boolean preImpactTest(Thing other) {
        dpx = 0;
        if (!Collision.hasPlayer(other.type)) return DO_IMPACT; // normal collision for anything but a player

        if (throwTimer > 0) {
            return SKIP_IMPACT;
        }
        if (thrown) return DO_IMPACT; // normal impact when flipped
        if (other.canLandOnTop(this)) {
            // Player is above us. adjust px to make it easy to stand on top
            Log.i("Shy", Boolean.toString(other.canLandOnTop(this)));
            dpx = px;
            px = clamp(other.px, px - radius, px + radius);
            dpx -= px;
        }

        return DO_IMPACT;
    }

    @Override
    public void postImpactTest() {
        radius = normalRadius;
        px += dpx;
    }

    @Override
    public void impactResolve(SimulationManager level, Thing other, boolean impacted) {
        if (!impacted) return;

        if (Collision.hasWall(other.type) && this.canLandOnTop(other)) {
            grounded = true;
        }
    }
}
