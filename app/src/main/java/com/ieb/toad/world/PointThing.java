package com.ieb.toad.world;

import com.ieb.toad.world.core.Camera;
import com.ieb.toad.world.core.Collision;
import com.ieb.toad.world.core.Thing;

import org.jetbrains.annotations.NotNull;

public class PointThing extends Thing {

    public PointThing() {
        type = Collision.SENSOR;
        radius = 1;
        gravity = 0.0;
    }

    /** Update point sensor location */
    public void locate(double x, double y){
        px = x;
        py = y;
    }

    @Override
    public void draw(@NotNull Camera camera, int frameMs) {}
}
