package com.ieb.toad.sprite;

import com.ieb.toad.sprite.core.Animation;
import com.ieb.toad.sprite.core.Flip;
import com.ieb.toad.sprite.core.SpriteSheetManager;
import com.ieb.toad.world.core.Camera;
import com.ieb.toad.world.core.Collision;
import com.ieb.toad.world.core.SimulationManager;
import com.ieb.toad.world.core.Thing;

import org.jetbrains.annotations.NotNull;

public class Coin extends Thing {
    private final Animation anim;
    private boolean collected;

    public Coin(SpriteSheetManager sprites) {
        anim = new Animation(100, Animation.FOREVER, sprites.stuff, Flip.None,
                new int[]{66,67,68,69,70,65,71});

        type = Collision.PASS_THROUGH;
        collected = false;
        radius = -1;
        mass = 0.8;
        gravity = 0.0; // not affected by gravity
    }

    @Override
    public void draw(@NotNull Camera camera) {
        camera.drawSprite(anim, px, py, 30);
    }

    @Override
    public int think(SimulationManager level, int ms) {
        if (collected) return REMOVE;

        anim.advance(ms);
        return KEEP;
    }

    @Override
    public void preImpactTest(Thing other) {
        // Only interact with player
        if (other.type != Collision.PLAYER) return;
        radius = 16.0;
    }

    @Override
    public void postImpactTest() {
        radius = -1.0;
    }

    @Override
    public void impactResolve(SimulationManager level, Thing other, boolean impacted) {
        if (!impacted) return;
        if (other.type != Collision.PLAYER) return;

        collected = true;
    }
}
