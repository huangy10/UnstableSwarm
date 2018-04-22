import processing.core.PVector;

public class Movable {
    PVector loc;
    PVector velocity;
    PVector acce;
    PVector force;
    PVector friction;
    // gravity should be global
    float mass;
    float frictionAcc;
    float noiseSeed;
    int id;

    static boolean enableGravity = false;
    static float boundaryGap = 100;

    float cachedProperty;

    // Movable should always has an id
    Movable(int id) {
        this.id = id;
        this.mass = 1;
        this.frictionAcc = 0.2f;
        this.noiseSeed = Sketch.getSK().random(0, 1000);

        this.loc = new PVector();
        this.velocity = new PVector();
        this.acce = new PVector();
        this.force = new PVector();
        this.friction = new PVector();
    }

    void update() {

    }

    void render() {

    }

    void applyNewtonForce() {
        velocity.add(acce);
        loc.add(velocity);
    }

    boolean outOfScreen() {
        Sketch sk = Sketch.getSK();
        return loc.x < - boundaryGap || loc.x > sk.width + boundaryGap ||
                loc.y < - boundaryGap || loc.y > sk.height + boundaryGap;
    }
}
