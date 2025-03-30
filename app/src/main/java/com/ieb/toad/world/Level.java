package com.ieb.toad.world;

import com.ieb.toad.Main;
import com.ieb.toad.sprite.core.SpriteSheetManager;
import com.ieb.toad.world.core.Camera;
import com.ieb.toad.world.core.Constraint;
import com.ieb.toad.world.core.SimulationManager;
import com.ieb.toad.world.core.Simulator;
import com.ieb.toad.world.core.Thing;
import com.ieb.toad.world.loader.LayerChunk;
import com.ieb.toad.world.loader.TiledLoader;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
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
    private final TiledLoader level;
    public final boolean loadedOk;

    public Level(Main context) throws IOException {
        spriteSheetManager = new SpriteSheetManager(context);
        simulator = new Simulator(this);
        sampleThing = new PointThing();
        level = new TiledLoader(context);


        loadedOk = level.loadLevel(0);


        things = new ArrayList<>();
        constraints = new ArrayList<>();

        //things.add(new Coin(spriteSheetManager));

        things.addAll(level.things);
    }

    public void Draw(@NotNull Camera camera, int width, int height, int frameMs) {
        camera.centreOn(level.toad.px, level.toad.py);

        // background

        // main
        Enumeration<LayerChunk> chunks = level.getMainChunks(camera.getCoverage()); // get layer image bits
        while (chunks.hasMoreElements()) {
            LayerChunk chunk = chunks.nextElement();
            camera.drawBitmap(chunk.getBitmap(), chunk.left, chunk.top, TiledLoader.SCALE);
        }

        for (Thing thing : things) {
            thing.draw(camera);
        }

        // foreground
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
        if (level.toad.py > 2000) {
            level.toad.px = 100;
            level.toad.py = 600;
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

    @Override
    public void removeThing(Thing t) {
        if (t.anyConstraints()){
            for (Constraint c : t.linkedConstraints()) {
                c.unlink();
                constraints.remove(c);
            }
        }
        things.remove(t);
    }

    public int getBackgroundColor() {
        return level.backgroundColor;
    }
}
