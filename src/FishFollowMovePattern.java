import processing.core.PVector;

public class FishFollowMovePattern extends MovePattern {
    static final FishFollowMovePattern defaultPattern = new FishFollowMovePattern();

    private static final float ATTRACTION = 0.05f;
    private static final float DAMPING = 0.05f;
    private static final float PERLIN_STRENGTH = 1.5f;
    private static final float BOUNDARY_AVOID_RANGE = -30;
    private static final float BOUNDARY_AVOID_STRENGTH = 0.5f;

    FishFollowMovePattern() {
        super();
        enableColorTransition = true;
        enableColorEasyIn = false;
        mostEnableDuration = 30 * 10;
    }

    @Override
    Sketch.State getState() {
        return Sketch.State.Fish;
    }

    @Override
    void update(Particle p) {
        //
        if (p.outOfScreen()) {
            p.reloc();
        }
        p.acce.set(0, 0);
        applyClusterLeaderAttraction(p, true);
        applyDamping(p);
        applyPerlinEngine(p);
        applyBoundaryAvoidance(p);
        swim(p);

//        p.velocity.mult(sk.random(0.9f, 1.0f));
        p.applyNewtonForce();
    }

    @Override
    void update(Swarm s) {
        if (s.outOfScreen()) {
            s.reloc();
            s.trace.clear();
        }
        pulse(s);
        s.updateTrace();
        s.acce.set(0, 0);
        applyClusterLeaderAttraction(s);
        applyDamping(s);
        applyPerlinEngine(s);
        applyBoundaryAvoidance(s);

        swim(s);
        s.applyNewtonForce();
    }

    @Override
    void render(Swarm s) {
        if (enableColorTransition && doSwithcColor && !s.isClusterLeader) {
            s.configColorFromParticle();
        }
        super.render(s);
    }

    void swim(Swarm s) {
        float swimForce;
        if (s.pulseCounter == 0)
            swimForce = s.isClusterLeader ? 0.4f * sk.perlinNoiseWithSeed(s.noiseSeed) : 0.2f;
        else
            swimForce = 0.6f;
        s.acce.add(s.velocity.copy().setMag(swimForce));
    }

    void swim(Particle p) {
        p.acce.add(p.velocity.copy().setMag(p.pulseCounter > 0 ? 0.4f : 0.2f));
    }

    void applyBoundaryAvoidance(Movable m) {
        if (m.loc.x < BOUNDARY_AVOID_RANGE) {
//            m.acce.add(BOUNDARY_AVOID_STRENGTH, 0);
            m.moveLoc(sk.width - BOUNDARY_AVOID_RANGE, 0);
        } else if (m.loc.x > sk.width - BOUNDARY_AVOID_RANGE) {
//            m.acce.add(- BOUNDARY_AVOID_STRENGTH, 0);
            m.moveLoc(-sk.width + BOUNDARY_AVOID_RANGE, 0);
        }

        if (m.loc.y < BOUNDARY_AVOID_RANGE) {
//            m.acce.add(0, BOUNDARY_AVOID_STRENGTH);
            m.moveLoc(0, sk.height - BOUNDARY_AVOID_RANGE);
        } else if (m.loc.y > sk.height - BOUNDARY_AVOID_RANGE) {
//            m.acce.add(0, -BOUNDARY_AVOID_STRENGTH);
            m.moveLoc(0, -sk.height + BOUNDARY_AVOID_RANGE);
        }
    }

    void applyPerlinEngine(Movable m) {
        PVector tmp = m.loc.copy().sub(sk.width / 2, sk.height / 2);
        float noise = sk.noise(tmp.mag(), tmp.heading(), sk.getTime()) - 0.5f;
        PVector perlin = m.velocity.copy().rotate(Sketch.PI / 2).setMag(PERLIN_STRENGTH * noise);
        m.acce.add(perlin);
    }

    void applyDamping(Movable m) {
        PVector damping = m.velocity.copy().mult(-DAMPING);
        m.acce.add(damping);
    }

    void applyClusterLeaderAttraction(Particle p, boolean followGlobalHead) {
        if (followGlobalHead) {
            for (Swarm clusterLead : p.swarm.clusterLeaders) {
                PVector d = PVector.sub(clusterLead.loc, p.loc);
                float dist = d.mag();
                if (dist < clusterLead.clusterAttractionRange / 2) {
                    continue;
                }
                PVector gForce = d.mult(ATTRACTION * clusterLead.mass * p.mass / dist / dist * 3);
                p.acce.add(gForce);
            }
        } else {
            Swarm clusterLead = p.swarm;
            PVector d = PVector.sub(clusterLead.loc, p.loc);
            float dist = d.mag();
            if (dist < clusterLead.clusterAttractionRange / 2) {
                return;
            }
            PVector gForce = d.mult(ATTRACTION * clusterLead.mass * p.mass / dist / dist * 3);
            p.acce.add(gForce);
        }
    }

    void applyClusterLeaderAttraction(Swarm s) {
        if (!s.isClusterLeader && s.clusterLeaders != null) {
            for (Swarm clusterLead: s.clusterLeaders) {
                PVector d = PVector.sub(clusterLead.loc, s.loc);
                float dist = d.mag();
                if (dist < clusterLead.clusterAttractionRange) {
                    continue;
                }
                PVector gForce = d.mult(ATTRACTION * clusterLead.mass * s.mass / dist / dist);
                s.acce.add(gForce);
            }
        }
    }

    void pulse(Swarm s) {
        if (!s.isClusterLeader) return;
        if (s.pulseCounter > 0) {
            s.pulseCounter -= 1;
        } else {
            if (sk.random(1) < 0.01f) {
                s.pulseCounter = (int) sk.frameRate;
            }
        }
    }

    @Override
    void patternIsEnabled() {
        super.patternIsEnabled();
        sk.relocateSwarms();
        switchColorBeginFC = sk.frameCount - 1;
    }
}
