package com.ieb.smalltest.world;

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

    /**
     * Fixed-step Leapfrog solver
     *
     * @param dt time elapsed since last call
     * @return Returns time advanced
     */
    public final double solve(double dt, Thing[] objects) {
        // We always solve to a fixed step-time,
        // but we change the number of steps
        // based on the frame time
        int iter = (int) ((speed * dt) / N); // number of iterations we will run
        double adv = ((double) iter * N) / speed; // the amount of simulation time this covers

        // Limit 'hidden' runs to prevent big jumps if frame timer stalls
        if (iter < 1) iter = 0;
        if (iter > 10) iter = 10;

        for (int i = 0; i < iter; i++) {
            for (int oi = 0; oi < objects.length; oi++) {
                Thing obj = objects[oi];
                if (obj.type == Collision.WALL) { // walls don't move
                    obj.v0x = obj.v0y = obj.v1x = obj.v1y = 0.0;
                    continue; // walls don't move
                }

                // Advance position
                obj.p1x = obj.p0x + (obj.v0x * h) + (0.5 * obj.a0x * h2);
                obj.p1y = obj.p0y + (obj.v0y * h) + (0.5 * obj.a0y * h2);

                // apply acceleration and constraints
                simulationStep(obj, objects, oi);

                // Advance velocity
                obj.v1x = obj.v0x + (0.5 * (obj.a0x + obj.a1x) * h);
                obj.v1y = obj.v0y + (0.5 * (obj.a0y + obj.a1y) * h);

                // Step values forward
                obj.v0x = obj.v1x;
                obj.v0y = obj.v1y;
                obj.p0x = obj.p1x;
                obj.p0y = obj.p1y;
                obj.a0x = obj.a1x;
                obj.a0y = obj.a1y;

                // Check terminal velocity
                double maxV2 = obj.terminalVelocity * obj.terminalVelocity;
                double v02 = (obj.v0x * obj.v0x) + (obj.v0y * obj.v0y);
                if (v02 > maxV2) { // need to restrict velocity
                    double adj = obj.terminalVelocity / Math.sqrt(v02);
                    obj.v0y *= adj;obj.v0x *= adj;
                    obj.v1y *= adj;obj.v1x *= adj;
                }
            }
        }
        return adv;
    }

    /**
     * Apply forces and constraints
     *
     * @param obj     the object under consideration
     * @param objects array of all objects
     * @param idx     index of the current object
     */
    private void simulationStep(Thing obj, Thing[] objects, int idx) {
        // Apply drag
        double drc = Math.max(0.0, 1.0 - obj.drag);
        obj.v0x *= drc;
        obj.v0y *= drc;

        // apply gravity
        obj.a1y = gravity * obj.gravity;

        // If radius is negative, there is no inter-object collision.
        // This object can still be the target of a collision (e.g. for walls)
        if (obj.radius < 0.0) return;

        // Check against other objects for collisions
        for (int i = 0; i < objects.length; i++) {
            if (i == idx) continue;
            Thing other = objects[i];

            other.preImpactTest(obj); // allow virtual impact point to be created

            if (other.radius <= 0.0) continue; // non-contact thing

            // if the other object has a hit circle,
            // do ball-to-ball calculations and affect both objects
            double dx = other.p1x - obj.p1x;
            double dy = other.p1y - obj.p1y;

            double r = other.radius + obj.radius;
            double rSqr = r * r;
            double dSqr = (dx * dx) + (dy * dy);

            boolean impacted = false;

            // First, do a cheap collision test
            if (dSqr < rSqr){ // objects are overlapping
                impacted = true;
                resolveCollision(obj, other, 0); // handle bounce
            } else if (dSqr <= rSqr * 2) { // if impact is likely
                double t = impactFraction(obj, other); // do exact collision test
                if (t > -h && t <= h) { // objects collide within a solver step
                    impacted = true;
                    resolveCollision(obj, other, t); // resolve collision
                }
            }

            other.postImpactResolve(obj, impacted);
        }
    }


    /**
     * Returns fraction of obj.v where impact occurs 0..1 if there is an impact.
     * This can return results out of the range if impact happens earlier or later.
     */
    public final double impactFraction(Thing obj, Thing other) {
        // Expansion of:
        //     "d = √( (o2.px + o2.vx * t - o1.px + o1.vx * t)² + (o2.py + o2.vy * t - o1.y + o1.vy * t)²)"
        // to solve in terms of 't' 0..1 where "d = o1.radius + o2.radius"
        double o1vx = obj.v0x, o1vy = obj.v0y, o1vx2 = o1vx * o1vx, o1vy2 = o1vy * o1vy;
        double o1x = obj.p0x, o1y = obj.p0y, o1x2 = o1x * o1x, o1y2 = o1y * o1y;
        double o1r = obj.radius, o1r2 = o1r * o1r;

        double o2vx = other.v0x, o2vy = other.v0y, o2vx2 = o2vx * o2vx, o2vy2 = o2vy * o2vy;
        double o2x = other.p0x, o2y = other.p0y, o2x2 = o2x * o2x, o2y2 = o2y * o2y;
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
    public final void resolveCollision(Thing obj, Thing other, double t) {
        pushApart(obj, other); // ensure we're not overlapping

        // calculate position of impact
        double ix1 = obj.p0x + obj.v0x * t;
        double iy1 = obj.p0y + obj.v0y * t;

        double ix2 = other.p0x + other.v0x * t;
        double iy2 = other.p0y + other.v0y * t;

        // calculate tangents of impact
        double nx = (ix1 - ix2) / (obj.radius + other.radius);
        double ny = (iy1 - iy2) / (obj.radius + other.radius);

        double a1 = obj.v0x * nx + obj.v0y * ny;
        double a2 = other.v0x * nx + other.v0y * ny;

        double p = 2 * (a1 - a2) / (obj.mass + other.mass);
        double coe = obj.elasticity * other.elasticity;

        // apply to velocity of both objects
        obj.v0x -= coe * p * nx * other.mass;
        obj.v0y -= coe * p * ny * other.mass;

        other.v0x += coe * p * nx * obj.mass;
        other.v0y += coe * p * ny * obj.mass;
    }

    /**
     * Check if there is an overlap between two objects
     */
    private void pushApart(Thing obj, Thing other) {
        double dx = obj.p0x - other.p0x;
        double dy = obj.p0y - other.p0y;
        double d2 = (dx * dx) + (dy * dy);
        double rs = obj.radius + other.radius;

        if (d2 > (rs * rs)) return;

        double d = Math.sqrt(d2); // current distance between centres
        if (d < rs) {
            double dd = rs - d; // overlap distance
            double frac = (dd / d); // fraction of current distance (dx,dy) to adjust
            double ms = obj.mass + other.mass;

            // push apart based on mass
            obj.p1x += dx * frac * (other.mass / ms);
            obj.p1y += dy * frac * (other.mass / ms);
            other.p1x -= dx * frac * (obj.mass / ms);
            other.p1y -= dy * frac * (obj.mass / ms);
        }
    }
}
