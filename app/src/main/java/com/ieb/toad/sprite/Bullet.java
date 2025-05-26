package com.ieb.toad.sprite;

import com.ieb.toad.world.core.Thing;

/** Bullets travel with o gravity until they hit a wall or the player.
 * They damage and despawn on player hit.
 * On wall hit, they gain gravity. Will bounce off the hit wall,
 * then fall with no collision until offscreen, then despawn. */
public class Bullet extends Thing {
    public Bullet(int direction, double x, double y) {

    }
}
