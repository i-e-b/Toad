package com.ieb.toad.sprite;

import com.ieb.toad.sprite.core.Animation;
import com.ieb.toad.sprite.core.Flip;
import com.ieb.toad.sprite.core.SpriteSheetManager;
import com.ieb.toad.world.core.Camera;
import com.ieb.toad.world.core.Collision;
import com.ieb.toad.world.core.SimulationManager;
import com.ieb.toad.world.core.Thing;

import org.jetbrains.annotations.NotNull;

public class Cherry extends Thing {
    private final Animation anim;
    private boolean collected;

    public Cherry(SpriteSheetManager sprites) {
        anim = new Animation(150, Animation.FOREVER, sprites.stuff, Flip.None,
                new int[]{84,85,86,87,86,85});
        //        new int[]{88,89,90,91,90,89}); <-- star

        type = Collision.COLLECTABLE;
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
