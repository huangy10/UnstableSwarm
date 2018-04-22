import processing.core.PGraphics;
import processing.core.PVector;

public class MovePattern {

    private static MovePattern globalEnabledPattern;
    Sketch sk;
    boolean enableRender = true;

    MovePattern() {
        sk = Sketch.getSK();
    }

    static MovePattern getGlobalEnabledPattern() {
        return globalEnabledPattern;
    }

    static void setGlobalEnabledPattern(MovePattern globalEnabledPattern) {
        if(!globalEnabledPattern.equals(MovePattern.globalEnabledPattern)) {
            globalEnabledPattern.patternIsEnabled();
            if (MovePattern.globalEnabledPattern != null)
                MovePattern.globalEnabledPattern.patternIsDisabled();
        }

        MovePattern.globalEnabledPattern = globalEnabledPattern;
    }

    void update(Particle p) {
    }

    void update(Swarm s) {
    }

    void render(Particle p) {
        PGraphics pg = sk.getParticleLayer();
        pg.stroke(p.defaultColor);
        pg.noFill();
        pg.strokeWeight(2);
//        pg.ellipse(p.loc.x, p.loc.y, 2, 2);
        pg.line(p.loc.x, p.loc.y, p.pLoc.x, p.pLoc.y);
    }

    void render(Swarm s) {
        if (!enableRender || s.trace.isEmpty()) {
            return;
        }
        PGraphics pg = sk.getSwarmLayer();
        pg.stroke(sk.lerpColor(s.stopColor, s.color, s.velocity.mag() / s.maxSpeed));
        pg.strokeWeight(s.headSize);
        pg.noFill();

        PVector pre = s.trace.getFirst();
        if (s.trace.size() == 1) {
            pg.point(pre.x, pre.y);
        } else {
            PVector p;
            for (int i = 1; i < s.trace.size(); i += 1) {
                pg.strokeWeight(Sketch.lerp(s.headSize, 1, (float) i / s.trace.size()));
                p = s.trace.get(i);
                pg.line(p.x, p.y, pre.x, pre.y);
                pre = p;
            }
        }
    }

    void renderLayers() {
        sk.image(sk.getParticleLayer(), 0, 0);
        sk.image(sk.getSwarmLayer(), 0, 0);
    }

    void patternIsEnabled() {

    }

    void patternIsDisabled() {

    }
}
