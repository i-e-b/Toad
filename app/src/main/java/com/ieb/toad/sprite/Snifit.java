package com.ieb.toad.sprite;

import com.ieb.toad.sprite.core.Animation;
import com.ieb.toad.sprite.core.Flip;
import com.ieb.toad.sprite.core.SpriteSheetManager;
import com.ieb.toad.sprite.kinds.WalkingCreep;
import com.ieb.toad.world.core.Camera;
import com.ieb.toad.world.core.Collision;

import org.jetbrains.annotations.NotNull;

public class Snifit extends WalkingCreep {
    private final Animation left;
    private final Animation right;
    private final Animation flipLeft;
    private final Animation flipRight;

    private double lastFramePx;

    public Snifit(final SpriteSheetManager sprites) {
        super(sprites);

        left = new Animation(160, Animation.FOREVER, sprites.dude, Flip.None, new int[]{2,3});
        right = new Animation(160, Animation.FOREVER, sprites.dude, Flip.Horz, new int[]{2,3});
        flipLeft = new Animation(160, Animation.FOREVER, sprites.dude, Flip.Vert, new int[]{2,3});
        flipRight = new Animation(160, Animation.FOREVER, sprites.dude, Flip.Horz + Flip.Vert, new int[]{2,3});

        canShoot = true;
        carried = thrown = false;
        type = Collision.CREEP;
        normalRadius = radius = 30;
        mass = 0.8;
        elasticity = 0.5;
        gravity = 1.0; // fully affected by gravity
    }

    @Override
    public void draw(@NotNull Camera camera, int frameMs) {
        double dx = Math.abs(px - lastFramePx);
        lastFramePx = px;

        Animation anim;
        if (carried || thrown){
            anim = desireDirection > 0 ? flipRight : flipLeft;
            anim.advance(frameMs); // animate based on time
        } else {
            anim = desireDirection > 0 ? right : left;
            anim.advance(dx*10); // animate based on movement
        }

        camera.drawSprite(anim, px, py, radius);
    }
}