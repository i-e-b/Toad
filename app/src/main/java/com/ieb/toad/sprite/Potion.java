package com.ieb.toad.sprite;

import com.ieb.toad.sprite.core.Animation;
import com.ieb.toad.sprite.core.Flip;
import com.ieb.toad.sprite.core.SpriteSheetManager;
import com.ieb.toad.world.core.Camera;
import com.ieb.toad.world.core.Collision;
import com.ieb.toad.world.core.SimulationManager;
import com.ieb.toad.world.core.Thing;

import org.jetbrains.annotations.NotNull;

public class Potion extends Thing {
    private final Animation grassAnim;
    private final Animation bottleAnim;
    private boolean collected;

    public Potion(SpriteSheetManager sprites) {
        grassAnim = new Animation(150, Animation.FOREVER, sprites.tiles,
                new int[]{520,521,522,523,524,525,526});
        bottleAnim = new Animation(150, Animation.FOREVER, sprites.stuff, Flip.None,
                new int[]{80,81,82,83});

        type = Collision.PASS_THROUGH;
        collected = false;
        radius = 16.0;
        mass = 0.8;
        gravity = 0.0; // not affected by gravity
    }

    @Override
    public void draw(@NotNull Camera camera, int frameMs) {
        camera.drawSprite(grassAnim, px, py, 0);
    }

    @Override
    public int think(SimulationManager level, int ms) {
        if (collected) return REMOVE;

        grassAnim.advance(ms);
        bottleAnim.advance(ms);
        return KEEP;
    }

    @Override
    public boolean preImpactTest(Thing other) {
        // Only interact with player
        return Collision.hasPlayer(other.type);
    }

    @Override
    public void impactResolve(SimulationManager level, Thing other, boolean impacted) {
        if (!impacted) return;
        if (other.type != Collision.PLAYER) return;

        //collected = true;
    }
}
