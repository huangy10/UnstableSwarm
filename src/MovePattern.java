import processing.core.PGraphics;
import processing.core.PVector;

public class MovePattern {

    private static MovePattern globalEnabledPattern;
    Sketch sk;
    boolean enableRender = true;
    boolean enableColorTransition = false;
    int activatedColorPlateIdx = -1;
    int switchColorInterval = 300;
    int switchColorSpeed = 30;
    int switchColorBeginFC = 0;
    boolean doSwithcColor = false;

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

    void update() {
        // update the move pattern it self
        if (enableColorTransition) {
            if (sk.frameCount - switchColorBeginFC >= switchColorInterval) {
                doSwithcColor = true;
                activatedColorPlateIdx = (int)sk.random(sk.colorPlate.length);
                switchColorBeginFC = sk.frameCount;
            } else {
                doSwithcColor = false;
            }
        }
    }

    void update(Particle p) {
    }

    void update(Swarm s) {
    }

    void render(Particle p) {
        PGraphics pg = sk.getParticleLayer();
        int strokeColor = p.defaultColor;
        if (enableColorTransition) {
            if (doSwithcColor) {
                // switch color
                p.pColor = p.color;
                int[] colorPlate = sk.colorPlate[activatedColorPlateIdx];
                p.color = colorPlate[(int) sk.random(colorPlate.length)];
                strokeColor = p.pColor;
            } else {
                float lerp = (sk.frameCount - switchColorBeginFC) / (float) switchColorSpeed;
                strokeColor = lerp > 1 ? p.color : sk.lerpColor(p.pColor, p.color, lerp);
            }
        } else {
            p.color = p.defaultColor;
        }
        pg.stroke(strokeColor);
        pg.noFill();
        pg.strokeWeight(2);
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
        doSwithcColor = false;
    }
}
