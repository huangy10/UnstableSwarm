import processing.core.PVector;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class ParticleMovePattern {

    private static ParticleMovePattern globalEnabledPattern;
    Sketch sk;
    boolean enableRender = true;

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
    }

    void update(Swarm s) {
    }

    void render(Particle p) {
        sk.fill(p.defaultColor);
        sk.noStroke();
        sk.ellipse(p.loc.x, p.loc.y, 2, 2);
    }

    void render(Swarm s) {
        if (!enableRender || s.trace.isEmpty()) {
            return;
        }

        sk.stroke(sk.lerpColor(s.stopColor, s.color, s.velocity.mag() / s.maxSpeed));
        sk.strokeWeight(s.headSize);
        sk.noFill();

        PVector pre = s.trace.getFirst();
        if (s.trace.size() == 1) {
            sk.point(pre.x, pre.y);
        } else {
            PVector p;
            for (int i = 1; i < s.trace.size(); i += 1) {
                sk.strokeWeight(Sketch.lerp(s.headSize, 1, (float) i / s.trace.size()));
                p = s.trace.get(i);
                sk.line(p.x, p.y, pre.x, pre.y);
                pre = p;
            }
        }
    }

    void patternIsEnabled() {

    }

    void patternIsDisabled() {

    }
}
