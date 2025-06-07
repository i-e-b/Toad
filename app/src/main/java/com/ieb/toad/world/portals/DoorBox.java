package com.ieb.toad.world.portals;

import android.graphics.Rect;

import com.ieb.toad.sprite.Key;
import com.ieb.toad.sprite.core.Animation;
import com.ieb.toad.sprite.core.SpriteSheetManager;
import com.ieb.toad.world.core.Camera;
import com.ieb.toad.world.core.Collision;
import com.ieb.toad.world.core.SimulationManager;
import com.ieb.toad.world.core.Thing;

import org.jetbrains.annotations.NotNull;

/**
 * Doors are non-colliding objects.
 * When the player presses "up" while touching one, they
 * swap between doors with the same target name, by ID.
 */
public class DoorBox extends DoorThing {
    private final Animation anim;
    private final Rect hitBox;

    private boolean locked;
    private boolean triggered;

    public DoorBox(SpriteSheetManager sprites, int left, int top, int width, int height, String target, boolean locked, int objId) {
        super(target, objId);

        anim = new Animation(1000, Animation.FOREVER, sprites.tiles, new int[]{266});

        this.locked = locked;
        hitBox = new Rect(left, top, left+width, top+height);

        layer = -2; // so the lock is behind Toad
        type = Collision.DOOR + Collision.PASS_THROUGH;
        radius = width / 2.0;
        px = hitBox.centerX();
        py = hitBox.centerY();
        triggered = false;
    }

    public boolean trigger(Key key){
        if (locked){
            if (key == null) return false;
            locked = false;
            key.keyUsed();
        }
        triggered = true;
        return true;
    }

    @Override
    public void draw(@NotNull Camera camera, int frameMs){
        if (locked){
            camera.drawSprite(anim, hitBox);
        }
    }

    public int think(SimulationManager level, int ms) {
        if (!triggered) return KEEP;

        // switch location
        triggered = false;
        level.moveNextDoor(target, objId);
        return KEEP;
    }

    @Override
    public void movePlayerToDoor(Thing t) {
        triggered = false;

        t.px = hitBox.centerX();
        t.py = hitBox.bottom - t.radius;
        t.vx = 0;
        t.vy = 0;
    }

    @Override
    public boolean preImpactTest(Thing other) {
        if (other.type != Collision.PLAYER) return SKIP_IMPACT;
        return DO_IMPACT;
    }
}
