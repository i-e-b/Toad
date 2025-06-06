package com.ieb.toad.sprite;

import com.ieb.toad.sprite.core.Animation;
import com.ieb.toad.sprite.core.Flip;
import com.ieb.toad.sprite.core.SpriteSheetManager;
import com.ieb.toad.world.core.Camera;
import com.ieb.toad.world.core.Collision;
import com.ieb.toad.world.core.SimulationManager;
import com.ieb.toad.world.core.Thing;

import org.jetbrains.annotations.NotNull;

/** Bullets travel with o gravity until they hit a wall or the player.
 * They damage and despawn on player hit.
 * On wall hit, they gain gravity. Will bounce off the hit wall,
 * then fall with no collision until offscreen, then despawn. */
public class Bullet extends Thing {

    private final Animation anim;

    private double lifeTimer;
    private boolean falling;

    private final double direction;
    private final double SPEED = 500;

    public Bullet(final SpriteSheetManager sprites, int direction, double x, double y) {
        anim = new Animation(1000, Animation.FOREVER, sprites.dude, Flip.None, new int[]{30});
        this.direction = direction;

        falling = false; // switched on hit
        lifeTimer = 10_000.0; // destroy bullet after timer expires, regardless of anything else
        type = Collision.BULLET + Collision.PASS_THROUGH; // pass through is removed on impact
        radius = 4.0;
        px = x;
        py = y + radius;
        mass = 0.8;
        elasticity = 0.9;
        drag = 0.001;
        gravity = 0.0; // gravity turned on after impact
    }

    @Override
    public int think(SimulationManager level, int ms) {
        ax = ay = 0;
        if (lifeTimer > 0.0) lifeTimer -= ms;
        else return REMOVE; // end of life

        if (!falling) vx = SPEED * direction;

        return KEEP;
    }

    @Override
    public void draw(@NotNull Camera camera, int frameMs) {
        camera.drawSprite(anim, px, py, radius);
    }

    @Override
    public boolean preImpactTest(Thing other) {
        if (falling) return DO_IMPACT; // bouncy 'dead' bullet;

        if (Collision.hasPlayer(other.type)) return DO_IMPACT; // normal collision for player
        if (Collision.hasWall(other.type)) return DO_IMPACT; // normal collision for wall

        return SKIP_IMPACT; // fly through anything else
    }

    @Override
    public void impactResolve(SimulationManager level, Thing other, boolean impacted) {
        if (!impacted) return;

        if (Collision.hasWall(other.type)) {
            // Hit a wall. Now we fall in the level as a dead object
            if (!falling) {
                vx /= 2.0;
                type = Collision.BULLET;
                falling = true;
                gravity = 1.0;
            }
        } else if (Collision.hasPlayer(other.type)) {
            // Hurt the player and disappear
            if (!falling) {
                lifeTimer = 0.0;
                level.damagePlayer();
            }
        }
    }
}
