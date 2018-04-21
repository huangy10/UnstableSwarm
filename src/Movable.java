import processing.core.PVector;

public class Movable {
    PVector loc;
    PVector velocity;
    PVector acce;
    PVector force;
    PVector friction;
    // gravity should be global
    static PVector gravityCenter;
    float mass;
    float frictionAcc;
    float noiseSeed;
    int id;

    static boolean enableGravity = false;
    static float gravityLevel = 1;
    static float stopThreshold = 0.01f;
    static float airFrictionThreshold = 3;

    // Movable should always has an id
    Movable(int id) {
        this.id = id;
        this.mass = 1;
        this.frictionAcc = 0.01f;
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

    void applyForce() {
        computeFriction();
        PVector a = PVector.add(force, friction).div(mass);
        velocity.add(a);
        loc.add(velocity);
        if (enableGravity) {
            applyGravity();
        }
    }

    void computeFriction() {
        float v = velocity.mag();
        float frictionStrength = frictionAcc;
        if (v < stopThreshold) {
            friction.set(force).setMag(-frictionAcc);
        } else {
            friction.set(velocity).setMag(
                    - frictionAcc - Math.max(0, v - airFrictionThreshold) / 5);
        }
    }

    void applyGravity() {
        float ringSlowDown = 0.5f;  // legacy magic number
        float ringContraintR = 0;
        PVector dx = PVector.sub(loc, gravityCenter).mult(ringSlowDown);
        float distance = dx.mag();
        float tmp = Math.abs(distance - ringContraintR) / Sketch.getSK().height / 2;

        dx.mult(1 - tmp * 0.3f);
        loc.set(gravityCenter).add(dx.div(ringSlowDown));
    }
}
