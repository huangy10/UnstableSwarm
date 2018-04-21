import com.sun.istack.internal.NotNull;
import jdk.nashorn.internal.objects.Global;
import processing.core.PApplet;
import processing.core.PVector;

import java.util.ArrayList;

public class Particle extends Movable {
    int defaultColor;
    PVector rootPos;    // root position which the particle should return
    Swarm swarm;

    static final float stopThreshold = 0.01f;
    static final float airFrictionThreshold = 3;

    Particle() {
        super(0);
        this.mass = 10;
        this.rootPos = new PVector();
    }

    @Override
    void update() {
        ParticleMovePattern.getGlobalEnabledPattern().update(this);
    }

    @Override
    void render() {
        ParticleMovePattern.getGlobalEnabledPattern().render(this);
    }
}
