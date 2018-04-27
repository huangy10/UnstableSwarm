import SimpleOpenNI.SimpleOpenNI;
import processing.core.PVector;

public class BodyMovePattern extends GatherMovePattern {

    static final BodyMovePattern defaultPattern = new BodyMovePattern();
    private static final float BOUNDARY_AVOID_RANGE = -30;

    KinectWrapper kinect;
    boolean doUpdateGoal = false;
    boolean doCapture = false;
    int goalUpdateRate = 5;
    boolean enableDebug = true;

    BodyMovePattern() {
        super();
        enableColorTransition = true;
        enableColorEasyIn = false;
        leastEnableInterval = 30 * 15;
        mostEnableDuration = 30 * 30;
    }

    void configure() {
        if (kinect == null) {
            kinect = new KinectWrapper(Sketch.getSK(), this);
            kinect.configureKinect();
        }
    }


    @Override
    protected boolean canBeDisabled() {
        return sk.frameCount - lastEnabledAtFC > leastEnableDuration;
    }

    @Override
    protected boolean canBeEnabled() {
        return sk.frameCount - lastDisabledAtFC > leastEnableInterval;
    }

    @Override
    void update() {
        doUpdateGoal = sk.frameCount % goalUpdateRate == 0;
        doCapture = sk.frameCount - lastEnabledAtFC > 150;
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
    protected void updateDurationControl() {
        // do nothing here
    }

    @Override
    void update(Particle p) {
        if (!kinect.isUserInScreen()) return;
        applyBoundaryAcross(p);
        updateGoal(p, p.goalPos);
        if(kinect.poly.contains(p.loc)) {
            super.update(p);
        } else {
            if (doCapture) {
                float x, y;
                int tries = 0;
                do {
                    x = sk.random(KinectWrapper.kinectWidth);
                    y = sk.random(KinectWrapper.kinectHeight);
                    tries += 1;
                    if (tries > 10) break;
                } while (!kinect.poly.contains(x, y));
                kinect.poly.coordConvertToScreen(x, y, p.loc);
                p.pLoc.set(p.loc);
            } else {
                runToGoal(p, p.goalPos);
            }
        }
    }

    @Override
    void stateAutoSwitch() {
        if (!kinect.isUserInScreen()) {
            MovePattern.setGlobalEnabledPattern(FishFollowMovePattern.defaultPattern);
        } else {
            super.stateAutoSwitch();
        }
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

    protected boolean updateGoal(Movable m, PVector goal) {
        if (!doUpdateGoal
                || kinect.poly.npoints == 0
                || kinect.poly.contains(m.loc)
                || kinect.poly.contains(goal)) {
            return false;
        }
        float x, y;
        int tries = 0;
        do {
            x = sk.random(KinectWrapper.kinectWidth);
            y = sk.random(KinectWrapper.kinectHeight);
            tries += 1;
            if (tries > 10) break;
        } while (!kinect.poly.contains(x, y));
        kinect.poly.coordConvertToScreen(x, y, goal);
        return true;
    }

    protected void runToGoal(Movable m, PVector goal) {
        PVector d = PVector.sub(goal, m.loc);
        d.limit(m.maxSpeed * 2);
        m.pLoc.set(m.loc);
        m.loc.add(d);
    }

    @Override
    void computeForce(Particle p) {
        float forceDir = sk.perlinNoiseWithSeed(p.noiseSeed / 100) * sk.PI * 6;
        float forceStrength = p.frictionAcc * 16;
        p.force.set(forceStrength * Sketch.cos(forceDir), forceStrength * Sketch.sin(forceDir));
        if (kinect.validBodyMove) {
            p.force.add(kinect.bodyMove.limit(p.frictionAcc * 32));
        }
    }

    @Override
    void applyGravity(Particle p) {
        // cancel gravity
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
        } else if (curId == -1) {
//            Sketch.println("Deactivate");
            MovePattern.setGlobalEnabledPattern(
                    LogoMovePattern.defaultPattern
            );
//            sk.switchToNRandomonkinectState();
        }
    }
}
