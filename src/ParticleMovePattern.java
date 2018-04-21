import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class ParticleMovePattern {

    private static ParticleMovePattern globalEnabledPattern;
    Sketch sk;

    ParticleMovePattern() {
        sk = Sketch.getSK();
    }

    static ParticleMovePattern getGlobalEnabledPattern() {
        return globalEnabledPattern;
    }

    static void setGlobalEnabledPattern(ParticleMovePattern globalEnabledPattern) {
        if(!globalEnabledPattern.equals(ParticleMovePattern.globalEnabledPattern)) {
            globalEnabledPattern.patternIsEnabled();
            if (ParticleMovePattern.globalEnabledPattern != null)
                ParticleMovePattern.globalEnabledPattern.patternIsDisabled();
        }

        ParticleMovePattern.globalEnabledPattern = globalEnabledPattern;
    }

    void update(Particle p) {
        throw new NotImplementedException();
    }

    void render(Particle p) {
        sk.fill(p.defaultColor);
        sk.noStroke();
        sk.ellipse(p.loc.x, p.loc.y, 2, 2);
    }

    void patternIsEnabled() {

    }

    void patternIsDisabled() {

    }
}
