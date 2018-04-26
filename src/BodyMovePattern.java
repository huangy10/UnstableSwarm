import SimpleOpenNI.SimpleOpenNI;
import processing.core.PVector;

public class BodyMovePattern extends MovePattern {

    static final BodyMovePattern defaultPattern = new BodyMovePattern();
    private static final float BOUNDARY_AVOID_RANGE = -30;

    KinectWrapper kinect;
    boolean doUpdateGoal = false;
    int goalUpdateRate = 5;
    boolean enableDebug = true;

    BodyMovePattern() {
        super();
        enableColorTransition = true;
        enableColorEasyIn = false;
    }

    void configure() {
        if (kinect == null) {
            kinect = new KinectWrapper(Sketch.getSK(), this);
            kinect.configureKinect();
        }
    }

    @Override
    void update() {
        doUpdateGoal = sk.frameCount % goalUpdateRate == 0;
        super.update();
    }

    @Override
    Sketch.State getState() {
        return Sketch.State.Body;
    }

    void updateKinect() {
        kinect.update(enableDebug || isGloballyEnabled());
    }

    @Override
    void update(Particle p) {
        applyBoundaryAcross(p);
        updateGoal(p, p.goalPos);
        runToGoal(p, p.goalPos);
    }

    @Override
    void update(Swarm s) {
        // do nothing here
    }

    @Override
    void render(Swarm s) {
        // do nothing
    }

    protected void applyBoundaryAcross(Movable m) {
        if (m.loc.x < BOUNDARY_AVOID_RANGE) {
            m.moveLoc(sk.width - BOUNDARY_AVOID_RANGE, 0);
        } else if (m.loc.x > sk.width - BOUNDARY_AVOID_RANGE) {
            m.moveLoc(-sk.width + BOUNDARY_AVOID_RANGE, 0);
        }

        if (m.loc.y < BOUNDARY_AVOID_RANGE) {
            m.moveLoc(0, sk.height - BOUNDARY_AVOID_RANGE);
        } else if (m.loc.y > sk.height - BOUNDARY_AVOID_RANGE) {
            m.moveLoc(0, -sk.height + BOUNDARY_AVOID_RANGE);
        }
    }

    protected void updateGoal(Movable m, PVector goal) {
        if (!doUpdateGoal
                || kinect.poly.npoints == 0
                || kinect.poly.contains(m.loc)
                || kinect.poly.contains(goal)) {
            return;
        }
        float x, y;
        do {
            x = sk.random(KinectWrapper.kinectWidth);
            y = sk.random(KinectWrapper.kinectHeight);
        } while (!kinect.poly.contains(x, y));
        kinect.poly.coordConvertToScreen(x, y, goal);
    }

    protected void runToGoal(Movable m, PVector goal) {
        PVector d = PVector.sub(goal, m.loc);
        d.limit(m.maxSpeed);
        m.pLoc.set(m.loc);
        m.loc.add(d);
    }

    protected void randomFlow(Movable m) {

    }

    void onNewUser(SimpleOpenNI curContext, int userId) {
        kinect.onNewUser(curContext, userId);
    }

    void onLostUser(SimpleOpenNI curContext, int userId) {
        kinect.onLostUser(curContext, userId);
    }

    void trackingUserChanged(int preId, int curId) {
        if (preId == -1 && curId != -1) {
            MovePattern.setGlobalEnabledPattern(this);
            Sketch.println("Activate");
        } else if (preId >= 0 && curId == -1) {
            Sketch.println("Deactivate");
        }
    }
}
