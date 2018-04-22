import processing.core.PApplet;
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
    boolean enableColorEasyIn = true;
    int colorEasyInLenght = 150;
    int defaultBgColor = 0xffffff;
    int bgColor = 0xffffff;
    int pBgColor = 0xffffff;

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

    void render() {
        int fillColor = getBgColor();
        Sketch.println(fillColor, defaultBgColor);
        sk.background(fillColor);
        PGraphics particleLayer = sk.getParticleLayer();
        particleLayer.beginDraw();
        particleLayer.noStroke();
        particleLayer.fill((fillColor & 0x00ffffff ) | (50 << 24));
//        particleLayer.fill(255, 50);
        particleLayer.rect(0, 0, sk.width, sk.height);

        PGraphics swarmLayer = sk.getSwarmLayer();
        swarmLayer.beginDraw();
        swarmLayer.clear();
    }

    int getBgColor() {
        if (enableColorTransition) {
            if (doSwithcColor) {
                pBgColor = bgColor;
                bgColor = sk.colorPlate[activatedColorPlateIdx][0];
                return bgColor;
            } else {
                float lerp = (float) (sk.frameCount - switchColorBeginFC) / (float) switchColorSpeed;
                return lerp > 1 ? bgColor : sk.lerpColor(pBgColor, bgColor, lerp);
            }
        } else if (enableColorEasyIn) {
            if (switchColorBeginFC == sk.frameCount) {
                pBgColor = bgColor;
                bgColor = defaultBgColor;
                return bgColor;
            } else {
                float lerp = (float) (sk.frameCount - switchColorBeginFC) / (float) colorEasyInLenght;
                return lerp > 1 ? bgColor : sk.lerpColor(pBgColor, bgColor, lerp);
            }
        }
        return defaultBgColor;
    }

    void render(Particle p) {
        PGraphics pg = sk.getParticleLayer();
        int strokeColor = p.defaultColor;
        if (enableColorTransition) {
            if (doSwithcColor) {
                // switch color
                p.pColor = p.color;
                int[] colorPlate = sk.colorPlate[activatedColorPlateIdx];
                p.color = colorPlate[(int) sk.random(colorPlate.length - 1) + 1];
                strokeColor = p.pColor;
            } else {
                float lerp = (float) (sk.frameCount - switchColorBeginFC) / (float) switchColorSpeed;
                strokeColor = lerp > 1 ? p.color : sk.lerpColor(p.pColor, p.color, lerp);
            }
        } else if (enableColorEasyIn) {
            if (switchColorBeginFC == sk.frameCount) {
                p.pColor = p.color;
                p.color = p.defaultColor;
                strokeColor = p.pColor;
            } else {
                float lerp = (float) (sk.frameCount - switchColorBeginFC) / (float) colorEasyInLenght;
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
        if (enableColorEasyIn) {
            switchColorBeginFC = sk.frameCount + 1;
        }
    }

    void patternIsDisabled() {
        doSwithcColor = false;
    }
}
