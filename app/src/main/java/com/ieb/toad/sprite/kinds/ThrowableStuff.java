package com.ieb.toad.sprite.kinds;

import com.ieb.toad.world.constraints.CarryingObject;
import com.ieb.toad.world.core.Collision;
import com.ieb.toad.world.core.SimulationManager;
import com.ieb.toad.world.core.Thing;

public class ThrowableStuff extends Creep {
    double dpx; // px value to restore after collision
    private double normalRadius;
    double recoverTimer,    // timer for getting up if we are thrown bt not carried
            throwTimer;     // timer to turn off collision for a short time after being thrown
    protected boolean carried, grounded, thrown;

    public ThrowableStuff(){
        type = Collision.CREEP;
        carried = thrown = grounded = false;
        drag = 0.0;
    }

    @Override
    public int think(SimulationManager level, int ms) {
        drag = grounded ? 0.1 : 0.0;

        if (carried){
            CarryingObject carry = (CarryingObject)getConstraint(CarryingObject.class);
            if (carry == null){ // we've beep dropped
                thrown();
            }
        } else {
            if (throwTimer > 0.0) throwTimer -= ms;

            // flip if on ground for a long time
            if (grounded && recoverTimer > 0.0){
                recoverTimer -= ms;
            }
            if (recoverTimer < 1.0){
                thrown = false;
            }
        }

        grounded = false; // will be reset if still on ground
        return KEEP;
    }

    @Override
    public void hitCreep(Thing hitBy) {

    }

    @Override
    public void thrown() {
        carried = false;
        thrown = true;

        recoverTimer = 5000.0; // time after stationary that creep flips back over
        throwTimer = 16.0; // time until collision is restored
    }

    @Override
    public void carried() {
        carried = true;
    }

    @Override
    public boolean preImpactTest(Thing other) {
        if (carried && Collision.hasPlayer(other.type)) return SKIP_IMPACT;

        if (throwTimer > 0 && Collision.hasPlayer(other.type)) {
            return SKIP_IMPACT;
        }

        normalRadius = radius;
        dpx = 0;
        if (!Collision.hasPlayer(other.type)) return DO_IMPACT; // normal collision for anything but a player

        if (throwTimer > 0) {
            return SKIP_IMPACT;
        }

        if (other.canLandOnTop(this)) {
            // Player is above us. adjust px to make it easy to stand on top
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

        if (thrown && !grounded && Collision.hasCreep(other.type)){
            // We hit another creep. Fall through walls, flip the other creep.
            if (other instanceof Creep){
                ((Creep)other).hitCreep(this);
            }
            hitCreep(other);
        }

        if (Collision.hasWall(other.type) && this.canLandOnTop(other)) {
            grounded = true;
        }
    }
}
