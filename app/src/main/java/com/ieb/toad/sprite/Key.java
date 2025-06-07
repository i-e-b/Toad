package com.ieb.toad.sprite;

import com.ieb.toad.sprite.core.Animation;
import com.ieb.toad.sprite.core.Flip;
import com.ieb.toad.sprite.core.SpriteSheetManager;
import com.ieb.toad.sprite.kinds.ThrowableStuff;
import com.ieb.toad.world.core.Camera;
import com.ieb.toad.world.core.Collision;
import com.ieb.toad.world.core.SimulationManager;

import org.jetbrains.annotations.NotNull;

public class Key extends ThrowableStuff {

    private final Animation anim;
    private final int spawnY;
    private final int spawnX;

    private boolean used;

    // TODO: need to respawn the key back at origin
    //  if it gets dropped in a pit (or any other despawn)

    public Key(final SpriteSheetManager sprites, int cx, int by) {
        super();

        spawnX = cx;
        spawnY = by;
        px = cx;
        py = by;
        anim = new Animation(1000, Animation.FOREVER, sprites.stuff, Flip.None, new int[]{36});

        type = Collision.CREEP;
        radius = 30;
        mass = 2.0;
        elasticity = 0.5;
        gravity = 1.0;
        used = false;
    }

    public void keyUsed(){
        used = true;
    }

    @Override
    public int think(SimulationManager level, int ms) {
        if (used) return REMOVE;
        return KEEP;
    }

    @Override
    public void draw(@NotNull Camera camera, int frameMs) {
        camera.drawSprite(anim, px, py, radius);
    }

    @Override
    public void despawned(SimulationManager level) {
        if (used) return;
        px = spawnX;
        py = spawnY;
        thrown = false;
        carried = false;
        level.addThing(this);
    }
}
