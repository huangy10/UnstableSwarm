import processing.core.PVector;

public class GatherMovePattern extends MovePattern {
    static final GatherMovePattern defaultPattern = new GatherMovePattern();
    PVector gravityCenter;
    float gravityStrength = 0.3f;

    GatherMovePattern() {
        super();
        enableRender = false;
    }

    @Override
    void patternIsEnabled() {
        updateDurationControl();
        super.patternIsEnabled();
    }

    protected void updateDurationControl() {
        leastEnableDuration = 30 * (int) sk.random(10, 20);
        mostEnableDuration = leastEnableDuration + 10;
    }

    @Override
    Sketch.State getState() {
        return Sketch.State.Gather;
    }

    @Override
    void update(Particle p) {
        computeForce(p);
        applyForce(p);

        if (p.loc.x < 0 || p.loc.y < 0 || p.loc.x > sk.width || p.loc.y > sk.height)
            p.reloc();
    }

    @Override
    void render(Swarm s) {
        if (enableRender) super.render(s);
    }

    void computeForce(Particle p) {
        float forceDir = sk.perlinNoiseWithSeed(p.noiseSeed) * sk.PI * 6;
        float forceStrength = p.frictionAcc * 3;
        p.force.set(forceStrength * Sketch.cos(forceDir), forceStrength * Sketch.sin(forceDir));
    }

    void applyForce(Particle p) {
        PVector pLoc = p.loc.copy();
        computeFriction(p);
        p.acce.set(PVector.add(p.force, p.friction).div(p.mass));
        p.applyNewtonForce();
        if (Particle.enableGravity) {
            applyGravity(p);
        }
        p.pLoc.set(pLoc);
    }

    void computeFriction(Particle p) {
        float v = p.velocity.mag();
        if (v < Particle.stopThreshold) {
            p.friction.set(p.force).setMag(-p.frictionAcc);
        } else {
            p.friction.set(p.velocity).setMag(
                    - p.frictionAcc - Math.max(0, v - Particle.airFrictionThreshold) / 5);
        }
    }

    void applyGravity(Particle p) {
        float ringSlowDown = 0.5f;  // legacy magic number
        float ringContraintR = 0;
        PVector dx = PVector.sub(p.loc, gravityCenter).mult(ringSlowDown);
        float distance = dx.mag();
        float tmp = Math.abs(distance - ringContraintR) / Sketch.getSK().height / 2;

        dx.mult(1 - tmp * 0.3f);
        p.loc.set(gravityCenter).add(dx.div(ringSlowDown));
    }
}
