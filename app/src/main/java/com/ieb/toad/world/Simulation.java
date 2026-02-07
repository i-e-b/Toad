package com.ieb.toad.world;

import android.graphics.Rect;
import android.util.Log;

import com.ieb.toad.world.core.Camera;
import com.ieb.toad.world.core.Constraint;
import com.ieb.toad.world.core.SimulationManager;
import com.ieb.toad.world.core.PhysicsEngine;
import com.ieb.toad.world.core.TRect;
import com.ieb.toad.world.core.Thing;
import com.ieb.toad.world.loader.LayerChunk;
import com.ieb.toad.world.loader.TiledLoader;
import com.ieb.toad.world.portals.DoorThing;

import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;

/**
 * A level with walls, creeps, and a player
 */
public class Simulation implements SimulationManager {
    private static final String TAG = "Simulation";

    /**
     * Set of things for the level
     */
    private final List<Thing> things; // TODO: better structure for larger levels
    private final List<Constraint> constraints; // TODO: better structure for larger levels
    private final PhysicsEngine physics;
    private final PointThing sampleThing; // Used for hit detection
    private final TiledLoader level;
    private transient Rect lastCheckpoint;
    private transient byte[] lastSaveState;

    private Camera lastCamera;

    public Simulation(TiledLoader level, List<Thing> things, List<Constraint> constraints) {
        physics = new PhysicsEngine(this);
        sampleThing = new PointThing();

        this.level = level;

        this.things = things;
        this.constraints = constraints;
        lastCheckpoint = level.toad.boundBox();
    }

    /** Save the level state to a byte array */
    public byte[] save(){
        try {
            var fos = new ByteArrayOutputStream();
            var oos = new ObjectOutputStream(fos);
            oos.writeObject(things);
            oos.writeObject(constraints);
            oos.close();
            fos.close();
            System.out.println("Object has been serialized");
            return fos.toByteArray();
        } catch (IOException e) {
            Log.e(TAG, "Failed to store check-point", e);
            return null;
        }
    }

    /** Restore level state from a byte array */
    public static Simulation restore(byte[] state, TiledLoader level){
        try {
            var fis = new ByteArrayInputStream(state);
            ObjectInputStream ois = new ObjectInputStream(fis);
            List<Thing> things = (List<Thing>) ois.readObject();
            List<Constraint> constraints = (List<Constraint>) ois.readObject();
            ois.close();
            fis.close();

            var result = new Simulation(level, things, constraints);

            return result;
        } catch (Exception e) {
            Log.e(TAG, "Failed to read check-point", e);
            return null;
        }
    }

    public Simulation(TiledLoader level) {
        physics = new PhysicsEngine(this);
        sampleThing = new PointThing();

        this.level = level;

        things = new ArrayList<>();
        constraints = new ArrayList<>();
        things.addAll(level.bgThings);
        things.addAll(level.doorThings);
        things.addAll(level.fgThings);
        things.sort(Comparator.comparing(o -> o.layer));
        lastCheckpoint = level.toad.boundBox();
    }

    public void Draw(@NotNull Camera camera, int frameMs) {
        lastCamera = camera;
        camera.centreOn(level.toad.px, level.toad.py, level.camZones);
        Rect coverage = camera.getCoverage();

        // Wipe to zone color, or level color if none set
        camera.clear(getBackgroundColor());

        // background
        drawLayer(camera, level.getBackgroundChunks(coverage), frameMs);

        // main
        drawLayer(camera, level.getMainChunks(coverage), frameMs);

        for (int i = 0; i < things.size(); i++) {
            Thing thing = things.get(i);
            thing.draw(camera, frameMs);
        }

        // foreground
        drawLayer(camera, level.getForegroundChunks(coverage), frameMs);
    }

    private void drawLayer(Camera camera, Enumeration<LayerChunk> chunks, int frameMs) {
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
        // TODO: skip physics if doing a transition animation
        // apply physics
        double time = (double) ms;
        double nextTime = physics.solve(time, things, constraints);

        // Check for checkpoint
        int tx = (int)level.toad.px;
        int ty = (int)level.toad.py;
        for (Rect checkpoint : level.checkpoints) {
            if (checkpoint.contains(tx,ty)) {
                // TODO: need to handle leaving this checkpoint and re-entering without hitting a different one
                if (checkpoint != lastCheckpoint){
                    var preTime = System.currentTimeMillis();
                    lastSaveState = this.save(); // TODO: kick up an event for this?
                    var saveTime = System.currentTimeMillis() - preTime;
                    Log.i(TAG, "Saved checkpoint in "+saveTime+"ms");
                }
                lastCheckpoint = checkpoint;
            }
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
            boolean hit = physics.hitTest(sampleThing, obj);
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
        // TODO: send to a graveyard, clear on next checkpoint
        if (t.anyConstraints()){
            for (Constraint c : t.linkedConstraints()) {
                c.unlink();
                constraints.remove(c);
            }
        }
        things.remove(t);
        things.sort(Comparator.comparing(o -> o.layer));
        t.despawned(this);
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
        next.movePlayerToDoor(level.toad); // stop doors triggering until control is released
    }

    @Override
    public void damagePlayer() {
        // TODO: animate damage (pop animation?)

        if (lastSaveState != null){
            var preTime = System.currentTimeMillis();
            var reSim = Simulation.restore(lastSaveState, level);
            var restoreTime = System.currentTimeMillis() - preTime;
            Log.i(TAG, "Save state restored in " + restoreTime + "ms");
            if (reSim != null) Log.i(TAG, "Things:"+reSim.things.size()+"; Constraints:"+reSim.constraints.size()+";");
            else Log.w(TAG, "restoration failed");
        }

        // Break all constraints and go back to last checkpoint
        level.toad.resetToCheckpoint(this, lastCheckpoint);
    }

    @Override
    public void addThing(Thing thing) {
        things.add(thing);
        things.sort(Comparator.comparing(o -> o.layer));
    }

    @Override
    public boolean isOnScreen(Thing thing) {
        if (lastCamera == null) return true;

        Rect coverage = lastCamera.getCoverage();
        int left = (int) (thing.px-thing.radius);
        int top = (int) (thing.py-thing.radius);
        int right = (int) (thing.px+thing.radius);
        int bottom = (int) (thing.py+thing.radius);

        if (top > coverage.bottom) return false;
        if (bottom < coverage.top) return false;
        if (right < coverage.left) return false;
        return left <= coverage.right;
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
