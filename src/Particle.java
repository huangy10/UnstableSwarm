import com.sun.istack.internal.NotNull;
import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;

public class Particle extends Movable {
    int defaultColor;
    int state;
    PVector rootPos;    // root position which the particle should return
    Swarm swarm;

    Particle() {
        super(0);
        this.rootPos = new PVector();
    }

    Particle(int id, @NotNull PVector root) {
        super(id);
        this.rootPos.set(root);
    }

    //
    @Override
    void update() {
        walkUpdate();
    }

    @Override
    void render() {
        Sketch sk = Sketch.getSK();
//        if (id == 0) {
//            Sketch.println(loc);
//        }
        sk.fill(defaultColor);
        sk.noStroke();
        sk.ellipse(loc.x, loc.y, 2, 2);
    }

    /// Define movable pattern
    private void walkUpdate() {
        Sketch sk = Sketch.getSK();
        float forceDir = sk.perlinNoiseWithSeed(noiseSeed) * sk.PI * 6;
        float forceStrength = frictionAcc * 6;
        force.set(forceStrength * Sketch.cos(forceDir), forceStrength * Sketch.sin(forceDir));
        applyForce();

        if (loc.x < 0 || loc.y < 0 || loc.x > sk.width || loc.y > sk.height) {
            loc.set(sk.random(sk.width), sk.random(sk.height));
        }
    }
}
