package com.ieb.toad.sprite;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.ieb.toad.sprite.core.Animation;
import com.ieb.toad.sprite.core.Flip;
import com.ieb.toad.sprite.core.SpriteSheetManager;
import com.ieb.toad.world.core.Camera;
import com.ieb.toad.world.core.Collision;
import com.ieb.toad.world.Level;
import com.ieb.toad.world.core.Thing;

import org.jetbrains.annotations.NotNull;

public class Shy extends Thing {
    private final Animation left;
    private final Animation right;

    private double lastFramePx;

    private int desireDirection = -1; // negative = left, positive = right.

    public final int SPEED = 1200, ACCEL = 5000;
    private double oldPx; // px value to restore after collision

    /** Load Toad graphics */
    public Shy(final SpriteSheetManager spriteSheetManager) {
        left = new Animation(16, Animation.FOREVER, spriteSheetManager.dude, Flip.None, new int[]{0,1});
        right = new Animation(16, Animation.FOREVER, spriteSheetManager.dude, Flip.Horz, new int[]{0,1});

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;

        type = Collision.CREEP;
        radius = 30;
        mass = 0.8;
        gravity = 1.0; // fully affected by gravity
    }

    @Override
    public void think(Level level, int ms) {
        // Switch direction if facing wall.
        var frontSense = level.hitTest(px + ((radius + 2) * desireDirection), py);
        if (Collision.hasWall(frontSense)) desireDirection = -desireDirection;

        if (desireDirection < 0){ // left
            if (vx > -SPEED) a0x = -ACCEL;
        } else { // right
            if (vx < SPEED) a0x = ACCEL;
        }
    }

    @Override
    public void draw(@NotNull Camera camera) {
        double dx = Math.abs(px - lastFramePx);
        lastFramePx = px;

        Animation anim = desireDirection > 0 ? right : left;
        anim.advance(dx); // animate based on movement

        camera.drawSprite(anim, px, py, radius);
    }

    private boolean restore = false;
    @Override
    public void preImpactTest(Thing other) {
        if (other.type != Collision.PLAYER) return; // normal collision for anything but a player
        if ((other.py+other.radius-1) > (py-radius+1)) return; // normal collision if player is not above us
        if (other.vy - vy < 0) return; // normal collision if player going up

        // Player is above us. adjust px to make it easy to stand on top
        restore = true;
        oldPx = px;
        px = clamp(other.px, px-radius, px+radius);
    }

    @Override
    public void postImpactResolve(Thing other, boolean impacted) {
        if (restore){
            px = oldPx;
            restore = false;
        }
    }
}
