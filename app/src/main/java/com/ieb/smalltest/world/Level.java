package com.ieb.smalltest.world;

import com.ieb.smalltest.Main;
import com.ieb.smalltest.sprite.Shy;
import com.ieb.smalltest.sprite.SpriteSheet;
import com.ieb.smalltest.sprite.Toad;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A level with walls, creeps, and a player
 */
public class Level {

    /**
     * Set of things for the level
     */
    private final List<Thing> things; // TODO: better structure for larger levels
    private final List<Constraint> constraints; // TODO: better structure for larger levels
    private final Simulator simulator;
    private final PointThing sampleThing; // Used for hit detection
    private final SpriteSheet spriteSheet;

    public Level(Main context) throws IOException {
        spriteSheet = new SpriteSheet(context);
        simulator = new Simulator(this);
        sampleThing = new PointThing();

        // TODO: text-to-level, like in Minikoban.
        things = new ArrayList<>();
        constraints = new ArrayList<>();

        things.add(new Toad(spriteSheet));
        things.get(0).p0x = 100;
        things.get(0).p0y = 600;

        things.add(new Shy(spriteSheet));
        things.get(1).p0x = 500;
        things.get(1).p0y = 750;

        things.add(new Platform(0, 500, 1000, 16));
        things.add(new Platform(0, 500, 16, 800));
        things.add(new Platform(0, 1300, 2000, 16));
        things.add(new Platform(1000, 500, 16, 800));

        things.add(new OneWayPlatform(0, 800, 128, 16));
        things.add(new OneWayPlatform(0, 1000, 128, 16));

        things.add(new ConveyorPlatform(450, 800, 350, 16, -6.0));

        things.add(new LifterPlatform(890, 1000, 32, 300, 700.0));
        things.add(new OneWayPlatform(850, 980, 112, 16));
    }

    public void Draw(@NotNull Camera camera, int width, int height, int frameMs) {
        camera.centreOn(things.get(0).p0x, things.get(0).p0y);
        
        for (Thing thing : things) {
            thing.draw(camera);
        }
    }


    /**
     * Run the level for up to `ms` milliseconds.
     * Returns number of milliseconds run.
     */
    public long stepMillis(long ms) {

        // apply physics
        double time = (double) ms;
        double nextTime = simulator.solve(time, things, constraints);

        // [TEMP] reset if out-of-bounds
        if (things.get(0).p0y > 2000) {
            things.get(0).p0x = 100;
            things.get(0).p0y = 600;
        }

        // return simulated time
        return (long) (nextTime);
    }

    /** Return what is at x,y on this level. Returns one of `Collision` */
    public int hitTest(double x, double y) {
        sampleThing.locate(x,y);
        int hits = 0;
        for (int oi = 0; oi < things.size(); oi++) {
            Thing obj = things.get(oi);

            obj.preImpactTest(sampleThing);
            boolean hit = simulator.hitTest(sampleThing, obj);
            obj.postImpactResolve(sampleThing, false);

            if (hit) hits = hits | obj.type;
        }
        return hits;
    }
}
