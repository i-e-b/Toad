package com.ieb.toad.world.constraints;

import com.ieb.toad.sprite.Toad;
import com.ieb.toad.world.core.Constraint;
import com.ieb.toad.world.core.Thing;

public class OnLadder extends Constraint {
    private final Toad player;
    private final Thing ladder;

    /** Sensor link between player and ladder */
    public OnLadder(Toad player, Thing ladder){
        this.player = player;
        this.ladder = ladder;

        player.linkConstraint(this);
    }

    @Override
    public int apply() {
        // Make sure the ground is under us
        ladder.preImpactTest(player);

        double dx = ladder.px - player.px;
        double adx = Math.abs(dx);
        double ady = Math.abs(ladder.py - player.py);

        // clean up
        ladder.postImpactTest();

        if (ady > player.radius) return BROKEN;
        if (adx > player.radius) return BROKEN;

        // if player is climbing, spring toward centre
        if (player.climbing) player.ax = Math.min(Math.max(dx * dx * dx * dx * dx, -700), 700);

        return OK;
    }

    @Override
    public void unlink() {
        player.unlinkConstraint(this);
    }
}
