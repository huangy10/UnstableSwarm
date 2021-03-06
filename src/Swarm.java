import processing.core.PVector;

import java.util.*;

public class Swarm extends Movable {
    private int size;
    Particle[] particles;
    boolean enabled = false;
    boolean isClusterLeader = false;
    float clusterAttractionRange = 50f;
    int color;
    int stopColor;
    float headSize = 8;

    LinkedList<PVector> trace;
    int traceSizeLimit = 5;

    List<Swarm> clusterLeaders;

    private Sketch sk;

    private static final float ATTRACTION = 0.05f;
    private static final float DAMPING = 0.05f;
    private static final float PERLIN_STRENGTH = 1.5f;
    private static final float BOUNDARY_AVOID_RANGE = 30;
    private static final float BOUNDARY_AVOID_STRENGTH = 0.5f;

    Swarm(int id, int size, boolean createParticles) {
        super(id);
        this.size = size;
        sk = Sketch.getSK();
        trace = new LinkedList<>();
        noiseSeed = sk.random(1000);
        maxSpeed = sk.random(5, 13);
        stopColor = sk.color(200, 50);
        color = sk.color(30, 50);
        mass = sk.random(20, 30);

        if (createParticles) {
            this.createParticles();
        }
    }

    void configFromParticle() {
        int idx = (int) sk.random(size);
        Particle p = particles[idx];
        updateLoc(p.loc);
        velocity.set(PVector.random2D().limit(0.1f));
        if (!isClusterLeader) {
            configColorFromParticle(p);
        }
    }

    void configColorFromParticle() {
        int idx = (int) sk.random(size);
        Particle p = particles[idx];
        configColorFromParticle(p);
    }

    void configColorFromParticle(Particle p) {
        color = p.color;
        stopColor = sk.color(
                sk.red(p.defaultColor),
                sk.green(p.defaultColor),
                sk.blue(p.defaultColor),
                sk.alpha(p.defaultColor) / 3);
    }

    @Override
    void update() {
        for (Particle p: particles) {
            p.update();
        }
        if (enabled) {
            MovePattern.getGlobalEnabledPattern().update(this);
        }

    }

    @Override
    void render() {
        for (Particle p: particles) {
            p.render();
        }

        if (enabled) {
            MovePattern.getGlobalEnabledPattern().render(this);
        }
    }

    void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            configFromParticle();
        } else {
            trace.clear();
        }
    }

    // create particles with current size
    private void createParticles() {
        particles = new Particle[size];
        for (int i = 0; i < size; i += 1) {
            particles[i] = new Particle();
            particles[i].swarm = this;
        }
    }

    void setAsAttractionHeader() {
        isClusterLeader = true;
        color = sk.color(255, 0, 0, 100);
        stopColor = sk.color(100, 0, 0);
        mass = 20f;
        clusterLeaders.add(this);
    }

    void updateTrace() {
        if (trace.size() > traceSizeLimit) {
            PVector p = trace.pollLast();
            if (p != null) {
                p.set(loc);
                trace.offerFirst(p);
            }
        } else {
            trace.offerFirst(loc.copy());
        }
    }

    @Override
    void moveLoc(float x, float y) {
        super.moveLoc(x, y);
        for (PVector p: trace) {
            p.add(x, y);
        }
    }
}
