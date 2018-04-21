import processing.core.PGraphics;
import processing.core.PVector;

import java.util.*;

public class Swarm extends Movable {
    private int size;
    Particle[] particles;
    boolean enabled = false;
    boolean isClusterLeader = false;
    float clusterAttractionRange = 100;
    int color;
    int stopColor;
    float headSize = 8;

    private LinkedList<PVector> trace;
    private int traceSizeLimit = 5;
    private float maxSpeed;
    private List<Swarm> clusterLeaders;

    private Sketch sk;
    private PGraphics pGraphics;

    private static final float ATTRACTION = 0.05f;
    private static final float DAMPING = 0.05f;
    private static final float PERLIN_STRENGTH = 1.5f;
    private static final float BOUNDARY_AVOID_RANGE = 20;
    private static final float BOUNDARY_AVOID_STRENGTH = 0.5f;

    Swarm(int id, int size, boolean createParticles) {
        super(id);
        this.size = size;
        sk = Sketch.getSK();
        trace = new LinkedList<>();
        noiseSeed = sk.random(1000);
        maxSpeed = sk.random(5, 13);
        stopColor = sk.color(30);
        color = sk.color(200);
        mass = sk.random(20, 30);

        if (createParticles) {
            this.createParticles();
        }
    }

    void configFromParticle() {
        loc.set(particles[0].loc);
        velocity.set(PVector.random2D().limit(0.1f));
    }

    @Override
    void update() {
        for (Particle p: particles) {
            p.update();
        }
        if (enabled) {
            updateSwim();
        }
    }

    @Override
    void render() {
        for (Particle p: particles) {
            p.render();
        }

        if (enabled) {
            renderSwarm();
        }
    }

    void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled) {
            loc.set(particles[0].loc);
            velocity.set(particles[0].velocity);
            acce.set(0, 0);
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

    private void renderSwarm() {
        if (trace.isEmpty()) return;
        sk.stroke(sk.lerpColor(stopColor, color, velocity.mag() / maxSpeed));
        sk.strokeWeight(headSize);
        sk.noFill();

        PVector pre = trace.getFirst();
        if (trace.size() == 1) {
            sk.point(pre.x, pre.y);
        } else {
            PVector p;
            for (int i = 1; i < trace.size(); i += 1) {
                sk.strokeWeight(Sketch.lerp(headSize, 1, (float) i / trace.size()));
                p = trace.get(i);
                sk.line(p.x, p.y, pre.x, pre.y);
                pre = p;
            }
        }
    }

    private void updateSwim() {
        if (outOfScreen()) {
            loc.set(sk.random(sk.width), sk.random(sk.height));
            trace.clear();
        }
        if (trace.size() > traceSizeLimit) {
            PVector p = trace.pollLast();
            if (p != null) {
                p.set(loc);
                trace.offerFirst(p);
            }
        } else {
            trace.offerFirst(loc.copy());
        }

        acce.set(0, 0);
        if (!isClusterLeader && clusterLeaders != null) {
            for (Swarm s: clusterLeaders) {
                applyClusterLeaderAttraction(s);
            }
        }
        applyDamping();
        applyPerlinEngine();
        applyBoundaryAvoidance();
        swim();

        velocity.add(acce).limit(maxSpeed);
        loc.add(velocity);
    }

    private void applyClusterLeaderAttraction(Swarm clusterLeader) {
        if (isClusterLeader) {
            return;
        }
        PVector d = PVector.sub(clusterLeader.loc, loc);
        float dist = d.mag();
        if (dist > clusterAttractionRange) {
            return;
        }

        PVector gForce = d.mult(ATTRACTION * clusterLeader.mass * mass / dist / dist);
        acce.add(gForce);
    }

    private void applyDamping() {
        PVector damp = velocity.copy().mult(-DAMPING);
        acce.add(damp);
    }

    private void applyPerlinEngine() {
        float noise = sk.noise(loc.mag(), loc.heading(), sk.getTime()) - 0.5f;
        PVector perlin = velocity.copy().rotate(Sketch.PI / 2).setMag(PERLIN_STRENGTH * noise);
        acce.add(perlin);
    }

    private void applyBoundaryAvoidance() {
        if (loc.x < BOUNDARY_AVOID_RANGE) {
            acce.add(BOUNDARY_AVOID_STRENGTH, 0);
        } else if (loc.x > sk.width - BOUNDARY_AVOID_RANGE) {
            acce.add(- BOUNDARY_AVOID_STRENGTH, 0);
        }

        if (loc.y < BOUNDARY_AVOID_RANGE) {
            acce.add(0, BOUNDARY_AVOID_STRENGTH);
        } else if (loc.y > sk.height - BOUNDARY_AVOID_RANGE) {
            acce.add(0, -BOUNDARY_AVOID_STRENGTH);
        }
    }

    private void swim() {
        float swimForce = isClusterLeader ? 0.4f * sk.perlinNoiseWithSeed(noiseSeed) : 0.2f;
        acce.add(velocity.copy().setMag(swimForce));
    }

    int getSize() {
        return size;
    }

    float getMaxSpeed() {
        return maxSpeed;
    }
}
