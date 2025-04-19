package com.ieb.toad.world;

import android.graphics.Rect;

import com.ieb.toad.Main;
import com.ieb.toad.world.core.Camera;
import com.ieb.toad.world.core.Constraint;
import com.ieb.toad.world.core.SimulationManager;
import com.ieb.toad.world.core.Simulator;
import com.ieb.toad.world.core.Thing;
import com.ieb.toad.world.loader.LayerChunk;
import com.ieb.toad.world.loader.TiledLoader;
import com.ieb.toad.world.portals.DoorThing;

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
    private final TiledLoader level;
    public final boolean loadedOk;
    private Rect lastCheckpoint;

    public Level(Main context) throws IOException {
        simulator = new Simulator(this);
        sampleThing = new PointThing();
        level = new TiledLoader(context);

        // TODO: show loading message, do this out of constructor
        loadedOk = level.loadLevel(0);

        things = new ArrayList<>();
        constraints = new ArrayList<>();
        things.addAll(level.bgThings);
        things.addAll(level.fgThings);
        things.addAll(level.doorThings);
        lastCheckpoint = level.toad.boundBox();
    }

    public void Draw(@NotNull Camera camera, int width, int height, int frameMs) {
        camera.centreOn(level.toad.px, level.toad.py, level.camZones);
        Rect coverage = camera.getCoverage();

        // background
        drawLayer(camera, level.getBackgroundChunks(coverage), frameMs);

        // main
        drawLayer(camera, level.getMainChunks(coverage), frameMs);

        for (int i = 0; i < things.size(); i++) {
            Thing thing = things.get(i);
            thing.draw(camera);
        }

        // foreground
        drawLayer(camera, level.getForegroundChunks(coverage), frameMs);
    }

    private void drawLayer(Camera camera, Enumeration<LayerChunk> chunks, int frameMs) {
        // TODO: need to handle animated tiles somehow
        if (chunks == null) return;
        while (chunks.hasMoreElements()) {
            LayerChunk chunk = chunks.nextElement();
            camera.drawBitmap(chunk.getBitmap(), chunk.left, chunk.top, TiledLoader.SCALE);
            chunk.advanceTime(frameMs);
        }
    }

    /**
     * Run the level for up to `ms` milliseconds.
     * Returns number of milliseconds run.
     */
    public long stepMillis(long ms) {
        if (!loadedOk) return ms;

        // TODO: skip physics if doing a transition animation
        // apply physics
        double time = (double) ms;
        double nextTime = simulator.solve(time, things, constraints);

        // Check for checkpoint
        int tx = (int)level.toad.px;
        int ty = (int)level.toad.py;
        for (Rect checkpoint : level.checkpoints) {
            if (checkpoint.contains(tx,ty)) lastCheckpoint = checkpoint;
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

    @Override
    public void moveNextDoor(String target, int srcObjId) {
        DoorThing lowest = null; // door with lowest ID and the same target
        DoorThing next = null; // lowest ID greater than src, with same target

        for (DoorThing door : level.doorThings) {
            if (!door.target.equals(target)) continue;
            if (door.objId == srcObjId) continue;

            if (lowest == null || lowest.objId > door.objId) lowest = door;
            if (door.objId > srcObjId){
                if (next == null || next.objId > door.objId) next = door;
            }
        }

        // chose next, or go back to first. If no target, do nothing.
        if (next == null) next = lowest;
        if (next == null) return;

        // move toad to new location
        next.moveAndHold(level.toad); // stop doors triggering until control is released
    }

    @Override
    public void damagePlayer() {
        // TODO: animate damage

        // Reset to last checkpoint
        level.toad.px = lastCheckpoint.centerX();
        level.toad.py = lastCheckpoint.bottom - level.toad.radius;
    }

    public int getBackgroundColor() {
        return level.backgroundColor;
    }

    public void backgroundUpdates(Camera camera) {
        Rect coverage = camera.getCoverage();

        // Update layer animations if required
        refreshIfDirty(level.getBackgroundChunks(coverage));
        refreshIfDirty(level.getMainChunks(coverage));
        refreshIfDirty(level.getForegroundChunks(coverage));
    }

    private void refreshIfDirty(Enumeration<LayerChunk> chunks) {
        if (chunks == null) return;
        while (chunks.hasMoreElements()) {
            LayerChunk chunk = chunks.nextElement();
            chunk.refreshIfDirty();
        }
    }
}
