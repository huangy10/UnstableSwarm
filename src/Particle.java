import processing.core.PVector;

public class Particle extends Movable {
    int defaultColor;
    PVector rootPos;    // root position which the particle should return
    Swarm swarm;

    int color;
    int pColor;
    int colorPlateIdx;

    static final float stopThreshold = 0.01f;
    static final float airFrictionThreshold = 3;

    Particle() {
        super(0);
        this.mass = 10;
        this.rootPos = new PVector();

        Sketch sk = Sketch.getSK();
        colorPlateIdx = (int) sk.random(sk.colorPlate[0].length);
    }

    @Override
    void update() {
        MovePattern.getGlobalEnabledPattern().update(this);
    }

    @Override
    void render() {
        MovePattern.getGlobalEnabledPattern().render(this);
    }
}
