package com.ieb.toad.world;

import com.ieb.toad.Main;
import com.ieb.toad.sprite.Shy;
import com.ieb.toad.sprite.core.SpriteSheetManager;
import com.ieb.toad.sprite.Toad;
import com.ieb.toad.world.core.Camera;
import com.ieb.toad.world.core.Constraint;
import com.ieb.toad.world.core.SimulationManager;
import com.ieb.toad.world.core.Simulator;
import com.ieb.toad.world.core.Thing;
import com.ieb.toad.world.platforms.ConveyorPlatform;
import com.ieb.toad.world.platforms.LifterPlatform;
import com.ieb.toad.world.platforms.OneWayPlatform;
import com.ieb.toad.world.platforms.Platform;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A level with walls, creeps, and a player
 */
public class Level implements SimulationManager {

    /**
     * Set of things for the level
     */
    private final List<Thing> things; // TODO: better structure for larger levels
    private final List<Constraint> constraints; // TODO: better structure for larger levels
    private final Simulator simulator;
    private final PointThing sampleThing; // Used for hit detection
    private final SpriteSheetManager spriteSheetManager;

    public Level(Main context) throws IOException {
        spriteSheetManager = new SpriteSheetManager(context);
        simulator = new Simulator(this);
        sampleThing = new PointThing();

        // TODO: text-to-level, like in Minikoban?
        things = new ArrayList<>();
        constraints = new ArrayList<>();

        things.add(new Toad(spriteSheetManager));
        things.get(0).px = 100;
        things.get(0).py = 600;

        things.add(new Shy(spriteSheetManager));
        things.get(1).px = 500;
        things.get(1).py = 750;

        things.add(new Platform(0, 500, 2000, 16)); // top
        things.add(new Platform(0, 500, 16, 800)); // left
        things.add(new Platform(0, 1300, 2000, 16)); // bottom
        things.add(new Platform(1984, 500, 16, 800)); // right

        things.add(new OneWayPlatform(0, 800, 128, 16));
        things.add(new OneWayPlatform(0, 1000, 128, 16));

        things.add(new ConveyorPlatform(450, 800, 350, 16, -6.0));
        things.add(new ConveyorPlatform(350, 1000, 350, 16, 10.0));

        things.add(new LifterPlatform(890, 1000, 32, 300, 700.0));
        things.add(new OneWayPlatform(850, 980, 112, 16));
    }

    public void Draw(@NotNull Camera camera, int width, int height, int frameMs) {
        camera.centreOn(things.get(0).px, things.get(0).py);
        
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
        if (things.get(0).py > 2000) {
            things.get(0).px = 100;
            things.get(0).py = 600;
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
            obj.postImpactTest();

            if (hit) hits = hits | obj.type;
        }
        return hits;
    }

    @Override
    public void addConstraint(Constraint c) {
        constraints.add(c);
    }

    @Override
    public void removeConstraint(Constraint c) {
        c.unlink();
        constraints.remove(c);
    }
}
