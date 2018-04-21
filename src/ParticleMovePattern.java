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
        ParticleMovePattern.globalEnabledPattern = globalEnabledPattern;
    }

    void update(Particle p) {
        throw new NotImplementedException();
    }

    void render(Particle p) {
        throw  new NotImplementedException();
    }
}
