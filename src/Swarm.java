import processing.core.PGraphics;
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
    float maxSpeed;
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
        loc.set(particles[0].loc);
        velocity.set(PVector.random2D().limit(0.1f));
    }

    @Override
    void update() {
        for (Particle p: particles) {
            p.update();
        }
        if (enabled) {
//            updateSwim();
            ParticleMovePattern.getGlobalEnabledPattern().update(this);
        }

    }

    @Override
    void render() {
        for (Particle p: particles) {
            p.render();
        }

        if (enabled) {
//            renderSwarm();
            ParticleMovePattern.getGlobalEnabledPattern().render(this);
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

    public void setAsAttractionHeader() {
        isClusterLeader = true;
        color = sk.color(255, 0, 0, 100);
        stopColor = sk.color(100, 0, 0);
        mass = 20f;
        clusterLeaders.add(this);
    }
}
