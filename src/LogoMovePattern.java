import processing.core.PVector;

public class LogoMovePattern extends GatherMovePattern {
    static final LogoMovePattern defaultPattern = new LogoMovePattern();
    int transCounter = 0;

    LogoMovePattern() {
        super();
        gravityStrength = 1f;
        enableRender = true;
        enableColorTransition = false;
        enableColorEasyIn = true;
    }

    @Override
    void patternIsEnabled() {
        super.patternIsEnabled();
        transCounter = (int) sk.frameRate * 3;
    }

    @Override
    void patternIsDisabled() {
        super.patternIsDisabled();
        transCounter = 0;
    }

    @Override
    void computeForce(Particle p) {
        if (transCounter > 0) {
            super.computeForce(p);
            transCounter -= 1;
        } else {
            PVector dir = PVector.sub(p.rootPos, p.loc);
            float distance = dir.mag();
            float forceStrength;
            if (distance > 20) {
                forceStrength = p.frictionAcc * 15;
            } else {
                forceStrength = Math.max(p.frictionAcc * 15 * distance / 20, 1f);
            }
            p.force.set(dir.setMag(forceStrength));
        }
    }

    @Override
    void update(Swarm s) {
        // Gather swarms into gravity Center
        PVector d = PVector.sub(gravityCenter, s.loc);
        float distance = d.mag();
        float forceStrength;

        s.updateTrace();

        if (distance > 20) {
            forceStrength = 0.2f;
        } else {
            forceStrength = Math.max(0.2f * distance / 20, 1f);
        }

        s.force.set(d.setMag(forceStrength * s.mass * 10));
        s.acce.set(s.force).div(s.mass);
        s.cachedProperty = distance;
        s.applyNewtonForce();
    }

    @Override
    void render(Swarm s) {
        float distance = s.cachedProperty;
        if (distance > 50) {
            super.render(s);
        }
    }
}