package com.ieb.toad.world;

import android.graphics.Rect;

import com.ieb.toad.world.core.Camera;
import com.ieb.toad.world.core.Collision;
import com.ieb.toad.world.core.Thing;

import org.jetbrains.annotations.NotNull;

public class PointThing extends Thing {

    public PointThing() {
        hitBox = new Rect(0,0,0,0);

        type = Collision.SENSOR;
        radius = 1;
        gravity = 0.0;
    }

    /** Update point sensor location */
    public void locate(double x, double y){
        px = x;
        py = y;
        hitBox.top = (int)y;
        hitBox.bottom = (int)y+1;
        hitBox.left = (int)x;
        hitBox.right = (int)x+1;
    }

    @Override
    public void draw(@NotNull Camera camera) {}

    @Override
    public void think(Level level, int ms) {}
}
