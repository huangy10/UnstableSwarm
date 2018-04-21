import processing.core.PVector;

public class LogoMovePattern extends GatherMovePattern {
    static final LogoMovePattern defaultPattern = new LogoMovePattern();
    int transCounter = 0;

    LogoMovePattern() {
        super();
        gravityStrength = 1f;
    }

    @Override
    void patternIsEnabled() {
        transCounter = (int) sk.frameRate * 3;
    }

    @Override
    void patternIsDisabled() {
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
}