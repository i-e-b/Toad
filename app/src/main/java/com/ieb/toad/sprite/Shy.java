package com.ieb.toad.sprite;

import com.ieb.toad.sprite.core.Animation;
import com.ieb.toad.sprite.core.Flip;
import com.ieb.toad.sprite.core.SpriteSheetManager;
import com.ieb.toad.world.core.Camera;
import com.ieb.toad.world.core.Collision;
import com.ieb.toad.world.core.SimulationManager;
import com.ieb.toad.world.core.Thing;

import org.jetbrains.annotations.NotNull;

public class Shy extends Thing {
    private final Animation left;
    private final Animation right;

    private double lastFramePx;

    private int desireDirection = -1; // negative = left, positive = right.

    public final int SPEED = 1200, ACCEL = 5000;
    private double dpx; // px value to restore after collision

    /** Load Toad graphics */
    public Shy(final SpriteSheetManager sprites) {
        left = new Animation(16, Animation.FOREVER, sprites.dude, Flip.None, new int[]{0,1});
        right = new Animation(16, Animation.FOREVER, sprites.dude, Flip.Horz, new int[]{0,1});

        type = Collision.CREEP;
        radius = 30;
        mass = 0.8;
        gravity = 1.0; // fully affected by gravity
    }

    @Override
    public int think(SimulationManager level, int ms) {
        // Switch direction if facing wall or pit
        var wallSense = level.hitTest(px + ((radius + 2) * desireDirection), py);
        if (Collision.hasWall(wallSense)) {
            desireDirection = -desireDirection; // turn on wall
        } else {
            var pitSense = level.hitTest(px + ((radius * 2) * desireDirection), py+radius+5);
            var groundSense = level.hitTest(px, py+radius+5);
            if (Collision.hasWall(groundSense) && !Collision.hasWall(pitSense))
                desireDirection = -desireDirection; // turn on pit, only if on ground
        }

        if (desireDirection < 0){ // left
            if (vx > -SPEED) a0x = -ACCEL;
        } else { // right
            if (vx < SPEED) a0x = ACCEL;
        }
        return KEEP;
    }

    @Override
    public void draw(@NotNull Camera camera) {
        double dx = Math.abs(px - lastFramePx);
        lastFramePx = px;

        Animation anim = desireDirection > 0 ? right : left;
        anim.advance(dx); // animate based on movement

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
