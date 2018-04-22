import processing.core.PVector;

public class FishFollowMovePattern extends ParticleMovePattern {
    static final FishFollowMovePattern defaultPattern = new FishFollowMovePattern();

    private static final float ATTRACTION = 0.05f;
    private static final float DAMPING = 0.05f;
    private static final float PERLIN_STRENGTH = 1.5f;
    private static final float BOUNDARY_AVOID_RANGE = 30;
    private static final float BOUNDARY_AVOID_STRENGTH = 0.5f;

    @Override
    void update(Particle p) {
        //
        if (p.outOfScreen()) {
            p.loc.set(sk.random(sk.width), sk.random(sk.height));
        }
        p.acce.set(0, 0);
        applyClusterLeaderAttraction(p, false);
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
            s.loc.set(sk.random(sk.width), sk.random(sk.height));
            s.trace.clear();
        }
        s.updateTrace();
        s.acce.set(0, 0);
        applyClusterLeaderAttraction(s);
        applyDamping(s);
        applyPerlinEngine(s);
        applyBoundaryAvoidance(s);

        swim(s);
        s.applyNewtonForce();
    }

    void swim(Swarm s) {
        float swimForce = s.isClusterLeader ? 0.4f * sk.perlinNoiseWithSeed(s.noiseSeed) : 0.2f;
        s.acce.add(s.velocity.copy().setMag(swimForce));
    }

    void swim(Particle p) {
        p.acce.add(p.velocity.copy().setMag(0.2f));
    }

    void applyBoundaryAvoidance(Movable m) {
        if (m.loc.x < BOUNDARY_AVOID_RANGE) {
            m.acce.add(BOUNDARY_AVOID_STRENGTH, 0);
        } else if (m.loc.x > sk.width - BOUNDARY_AVOID_RANGE) {
            m.acce.add(- BOUNDARY_AVOID_STRENGTH, 0);
        }

        if (m.loc.y < BOUNDARY_AVOID_RANGE) {
            m.acce.add(0, BOUNDARY_AVOID_STRENGTH);
        } else if (m.loc.y > sk.height - BOUNDARY_AVOID_RANGE) {
            m.acce.add(0, -BOUNDARY_AVOID_STRENGTH);
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
                if (dist < clusterLead.clusterAttractionRange) {
                    continue;
                }
                PVector gForce = d.mult(ATTRACTION * clusterLead.mass * p.mass / dist / dist * 2);
                p.acce.add(gForce);
            }
        } else {
            Swarm clusterLead = p.swarm;
            PVector d = PVector.sub(clusterLead.loc, p.loc);
            float dist = d.mag();
            if (dist < clusterLead.clusterAttractionRange / 2) {
                return;
            }
            PVector gForce = d.mult(ATTRACTION * clusterLead.mass * p.mass / dist / dist * 2);
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

    @Override
    void patternIsEnabled() {
        sk.relocateSwarms();
    }
}
