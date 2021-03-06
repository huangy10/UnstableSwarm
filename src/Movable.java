import processing.core.PVector;

public class Movable {
    PVector loc, pLoc;
    PVector velocity;
    PVector acce;
    PVector force;
    PVector friction;
    // gravity should be global
    float mass;
    float frictionAcc;
    float noiseSeed;
    int id;
    float maxSpeed;

    static boolean enableGravity = false;
    static float boundaryGap = 500;
    int pulseCounter = 0;

    float cachedProperty;

    // Movable should always has an id
    Movable(int id) {
        this.id = id;
        this.mass = 1;
        this.frictionAcc = 0.2f;
        this.noiseSeed = Sketch.getSK().random(0, 1000);

        this.loc = new PVector();
        this.pLoc = new PVector();
        this.velocity = new PVector();
        this.acce = new PVector();
        this.force = new PVector();
        this.friction = new PVector();
        this.maxSpeed = Sketch.getSK().random(10, 15);;
    }

    void update() {

    }

    void render() {

    }

    void applyNewtonForce() {
        if (maxSpeed == 0) {
            velocity.add(acce);
        } else {
            velocity.add(acce).limit(maxSpeed);
        }
        updateLoc();
    }

    boolean outOfScreen() {
        Sketch sk = Sketch.getSK();
        return loc.x < - boundaryGap || loc.x > sk.width + boundaryGap ||
                loc.y < - boundaryGap || loc.y > sk.height + boundaryGap;
    }

    void reloc() {
        Sketch sk = Sketch.getSK();
        loc.set(sk.random(sk.width), sk.random(sk.height));
        pLoc.set(loc);
    }

    void relocCenter() {
        Sketch sk = Sketch.getSK();
        loc.set(sk.width / 2, sk.height / 2);
        pLoc.set(loc);
    }

    void updateLoc(PVector l) {
        pLoc.set(loc);
        loc.set(l);
    }

    void updateLoc(float x, float y) {
        pLoc.set(loc);
        loc.set(x, y);
    }

    void updateLoc() {
        pLoc.set(loc);
        loc.add(velocity);
    }

    void moveLoc(float x, float y) {
        pLoc.add(x, y);
        loc.add(x, y);
    }
}
