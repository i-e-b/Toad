package com.ieb.toad.world.core;

import com.ieb.toad.world.Level;

import java.util.List;

/**
 * Runs a leapfrog integrator for the physics
 */
public class Simulator {

    /**
     * steps per time period
     */
    public static final double N = 60 * 4;

    /**
     * speed of simulation
     */
    public static final double speed = 60;

    /**
     * step size parameter
     */
    public static final double h = 1.0 / N;

    /**
     * h²
     */
    public static final double h2 = h * h;

    /**
     * Gravity acceleration
     */
    public static final double gravity = 2500.0;

    /** Container for objects in the simulation */
    private final Level level;

    /** Count of iterations since last 'think' round */
    private int thinkTrigger = 0;

    public Simulator(Level level) {
        this.level = level;
    }

    /**
     * Fixed-step Leapfrog solver
     *
     * @param dt time elapsed since last call
     * @return Returns time advanced
     */
    public final double solve(double dt, List<Thing> objects, List<Constraint> constraints) {
        // We always solve to a fixed step-time,
        // but we change the number of steps
        // based on the frame time
        int iterations = (int) ((speed * dt) / N); // number of iterations we will run
        double adv = ((double) iterations * N) / speed; // the amount of simulation time this covers
        int thinkAdv = (int) (((double) 10 * N) / speed); // the amount of simulation time per 'think' cycle in ms

        // Limit 'hidden' runs to prevent big jumps if frame timer stalls
        if (iterations < 1) iterations = 0;
        if (iterations > 10) iterations = 10;

        for (int i = 0; i < iterations; i++) {
            // Run the iteration on objects
            for (int oi = 0; oi < objects.size(); oi++) {
                Thing obj = objects.get(oi);

                // Advance position
                obj.px += (obj.vx * h) + (0.5 * obj.a0x * h2);
                obj.py += (obj.vy * h) + (0.5 * obj.a0y * h2);

                // apply acceleration and constraints
                simulationStep(obj, objects, oi);

                // Advance velocity
                obj.vx += (0.5 * (obj.a0x + obj.ax) * h);
                obj.vy += (0.5 * (obj.a0y + obj.ay) * h);

                // Step values forward
                obj.a0x = obj.ax;
                obj.a0y = obj.ay;

                // Check terminal velocity
                double maxV2 = obj.terminalVelocity * obj.terminalVelocity;
                double v02 = (obj.vx * obj.vx) + (obj.vy * obj.vy);
                if (v02 > maxV2) { // need to restrict velocity
                    double adj = obj.terminalVelocity / Math.sqrt(v02);
                    obj.vx *= adj;
                    obj.vy *= adj;
                }
            }

            // Apply all constraints
            for (int ci = 0; ci < constraints.size(); ci++){
                Constraint c = constraints.get(ci);
                c.apply();
            }

            // Do think round if triggered
            if (thinkTrigger++ > 9){
                for (int oi = 0; oi < objects.size(); oi++) {
                    objects.get(oi).think(level, thinkAdv);
                }
                thinkTrigger = 0;
            }
        }
        return adv;
    }

    /**
     * Apply forces and collisions
     *
     * @param self     the object under consideration
     * @param objects array of all objects
     * @param idx     index of the current object. This must be called in order.
     */
    private void simulationStep(Thing self, List<Thing> objects, int idx) {
        // Apply drag
        double drc = Math.max(0.0, 1.0 - self.drag);
        self.vx *= drc;
        self.vy *= drc;

        // apply gravity
        self.ay = gravity * self.gravity;

        // Check against other objects for collisions
        for (int i = idx+1; i < objects.size(); i++) {
            Thing other = objects.get(i);

            other.preImpactTest(self); // allow virtual impact point to be created
            self.preImpactTest(other);

            boolean impacted = false;
            if (other.radius>0 && self.radius > 0) {
                double time = impactTime(self, other);

                if (time < 0) { // objects are overlapping
                    impacted = true;
                    pushApart(self, other); // ensure we're not overlapping
                    resolveCollision(self, other, 0); // handle bounce as if at surface
                } else if (time <= h) { // objects will impact within a simulator frame
                    impacted = true;
                    resolveCollision(self, other, time); // resolve collision forward in time
                }
            }

            other.postImpactResolve(self, impacted);
            self.postImpactResolve(other, impacted);
        }
    }

    /** Returns true if two objects are touching, or would collide in the next simulator frame */
    public boolean hitTest(Thing obj, Thing other){
        return impactTime(obj, other) <= h;
    }

    private double impactTime(Thing obj, Thing other) {
        // if the other object has a hit circle,
        // do ball-to-ball calculations and affect both objects
        double dx = other.px - obj.px;
        double dy = other.py - obj.py;

        double r = other.radius + obj.radius;
        double rSqr = r * r;
        double dSqr = (dx * dx) + (dy * dy);

        // First, do a cheap collision test
        if (dSqr < rSqr){ // objects are overlapping
            return -1;
        } else if (dSqr <= rSqr * 2) { // if impact is likely
            double t = impactFraction(obj, other); // do exact collision test
            if (t > 0 && t <= h) { // objects collide within a solver step
                return t;
            }
        }
        return 1000.0; // just a large value to say no-impact
    }

    /**
     * Returns fraction of obj.v where impact occurs 0..1 if there is an impact.
     * This can return results out of the range if impact happens earlier or later.
     */
    public final double impactFraction(Thing obj, Thing other) {
        // Expansion of:
        //     "d = √( (o2.px + o2.vx * t - o1.px + o1.vx * t)² + (o2.py + o2.vy * t - o1.y + o1.vy * t)²)"
        // to solve in terms of 't' 0..1 where "d = o1.radius + o2.radius"
        double o1vx = obj.vx, o1vy = obj.vy, o1vx2 = o1vx * o1vx, o1vy2 = o1vy * o1vy;
        double o1x = obj.px, o1y = obj.py, o1x2 = o1x * o1x, o1y2 = o1y * o1y;
        double o1r = obj.radius, o1r2 = o1r * o1r;

        double o2vx = other.vx, o2vy = other.vy, o2vx2 = o2vx * o2vx, o2vy2 = o2vy * o2vy;
        double o2x = other.px, o2y = other.py, o2x2 = o2x * o2x, o2y2 = o2y * o2y;
        double o2r = other.radius, o2r2 = o2r * o2r;

        double a = o1vx2 + o1vy2 - 2 * o1vx * o2vx + o2vx2 - 2 * o1vy * o2vy + o2vy2;
        double b = -o1x * o1vx - o1y * o1vy + o1vx * o2x + o1vy * o2y + o1x * o2vx - o2x * o2vx + o1y * o2vy - o2y * o2vy;
        double c = o1x2 + o1y2 - o1r2 - 2 * o1x * o2x + o2x2 - 2 * o1y * o2y + o2y2 - 2 * o1r * o2r - o2r2;

        double n2b = b * -2;
        double discriminant = (n2b * n2b) - 4 * a * c;

        double dSqrt = Math.sqrt(discriminant);
        double t = Math.min(
                0.5 * (2 * b - dSqrt) / a,
                0.5 * (2 * b + dSqrt) / a
        );
        return Math.max(0, t);
    }

    /**
     * Bounce two objects, by changing velocity. Objects should be in contact
     *
     * @param other object 2
     * @param t     fraction of object velocities where impact occurs
     */
    private void resolveCollision(Thing obj, Thing other, double t) {
        // calculate position of impact
        double ix1 = obj.px + obj.vx * t;
        double iy1 = obj.py + obj.vy * t;

        double ix2 = other.px + other.vx * t;
        double iy2 = other.py + other.vy * t;

        // calculate tangents of impact
        double nx = (ix1 - ix2) / (obj.radius + other.radius);
        double ny = (iy1 - iy2) / (obj.radius + other.radius);

        double a1 = obj.vx * nx + obj.vy * ny;
        double a2 = other.vx * nx + other.vy * ny;

        double p = 2 * (a1 - a2) / (obj.mass + other.mass);
        double coe = obj.elasticity * other.elasticity;

        // apply to velocity of both objects
        obj.vx -= coe * p * nx * other.mass;
        obj.vy -= coe * p * ny * other.mass;

        other.vx += coe * p * nx * obj.mass;
        other.vy += coe * p * ny * obj.mass;
    }

    /**
     * Check if there is an overlap between two objects
     */
    private void pushApart(Thing obj, Thing other) {
        double dx = obj.px - other.px;
        double dy = obj.py - other.py;
        double d2 = (dx * dx) + (dy * dy);
        double rs = obj.radius + other.radius;

        if (d2 >= (rs * rs)) return;

        double d = Math.sqrt(d2); // current distance between centres
        if (d < rs) {
            double dd = rs - d; // overlap distance
            double frac = (dd / d); // fraction of current distance (dx,dy) to adjust
            double ms = obj.mass + other.mass;

            // push apart based on mass
            obj.px += dx * frac * (other.mass / ms);
            obj.py += dy * frac * (other.mass / ms);
            other.px -= dx * frac * (obj.mass / ms);
            other.py -= dy * frac * (obj.mass / ms);
        }
    }
}
