import SimpleOpenNI.*;
import blobDetection.*;
import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PVector;


public class KinectWrapper {
    static float kinectWidth = 640f;
    static float kinectHeight = 480f;

    private SimpleOpenNI context;
    private BlobDetection blobDetection;
    private int currentTrackingUserId = -1;
    private int startTrackingFC = 0;
    private PVector bodyPos;
    boolean validBodyMove;
    PVector bodyMove;

    private float scale;
    private float offset = 0;
    private PImage blobProcessImage;
    private int step = 2;
    Sketch sk;
    PolygonBlob poly;

    private BodyMovePattern bodyMovePattern;

    KinectWrapper(PApplet applet, BodyMovePattern pattern) {
        context = new SimpleOpenNI(applet);
        sk = Sketch.getSK();
        poly = new PolygonBlob(this);
        bodyMovePattern = pattern;
        bodyMove = new PVector();
        bodyPos = new PVector();
        validBodyMove = false;
    }

    boolean configureKinect() {
        if (!context.isInit()) {
            Sketch.println("Can not init Kinect");
            return false;
        }
        context.setMirror(true);

        context.enableDepth();
        context.enableUser();

        scale = (float) sk.height / kinectHeight;
        offset = (int)(sk.width - KinectWrapper.kinectWidth * scale) / 2;
        blobProcessImage = sk.createImage((int) kinectWidth / step, (int) kinectHeight / step, sk.RGB);
        blobDetection = new BlobDetection(blobProcessImage.width, blobProcessImage.height);
        blobDetection.setThreshold(0.2f);
        return true;
    }

    void update(boolean actively) {
        context.update();
        int[] usersList = context.getUsers();

        if (currentTrackingUserId >= 0 &&
                sk.frameCount - startTrackingFC > 300 &&
                usersList.length > 1) {
            // 超时更换用户
            int preId = currentTrackingUserId;
            currentTrackingUserId = usersList[(int) sk.random(0, usersList.length)];
            if (preId != currentTrackingUserId) {
                trackingUserChanged(preId, currentTrackingUserId);
            }
        }

        if (currentTrackingUserId >= 0) {
            // actively enable this status
            MovePattern.setGlobalEnabledPattern(
                    BodyMovePattern.defaultPattern
            );
        }

        if (actively) {
            int[] userMap = context.userMap();
            int idx, blobIdx;
            blobProcessImage.loadPixels();
            for (int i = step * 3; i < blobProcessImage.width - step * 3; i += 1) {
                for (int j = step * 3; j < blobProcessImage.height - step * 3; j += 1) {
                    blobIdx = i + j * blobProcessImage.width;
                    idx = i * step + j * context.depthWidth() * step;
                    if (userMap[idx] == currentTrackingUserId) {
                        blobProcessImage.pixels[blobIdx] = sk.color(255);
                    } else {
                        blobProcessImage.pixels[blobIdx] = sk.color(0);
                    }
                }
            }
            blobProcessImage.filter(sk.BLUR);
            blobDetection.computeBlobs(blobProcessImage.pixels);
            poly.reset();
            poly.createPolygon();

            if(currentTrackingUserId >= 0) {
                PVector com = new PVector();
                if (context.getCoM(currentTrackingUserId, com)) {
                    if (validBodyMove) {
                        bodyMove = PVector.sub(com, bodyPos);
                    } else {
                        validBodyMove = true;
                        bodyPos.set(com);
                    }
                } else {
                    validBodyMove = false;
                }
            }
        }
    }

    void debugRender() {
        sk.image(blobProcessImage, 0, 0);
    }

    void trackingUserChanged(int preId, int curId) {
        Sketch.print("Tracking user changed ");
        Sketch.println(preId, curId);
        startTrackingFC = sk.frameCount;
        validBodyMove = false;
        bodyMovePattern.trackingUserChanged(preId, curId);
    }

    void onNewUser(SimpleOpenNI curContext, int userId) {
        Sketch.println("OnNewUser: " + userId);
        if (currentTrackingUserId == -1) {
            currentTrackingUserId = userId;
            trackingUserChanged(-1, userId);
        }
        curContext.startTrackingSkeleton(userId);
    }

    void onLostUser(SimpleOpenNI curContext, int userId) {
        Sketch.println("OnLostUser: " + userId);
        if (userId == currentTrackingUserId) {
            int[] otherUsers = curContext.getUsers();
            int preId = currentTrackingUserId;
            if (otherUsers.length > 1) {
                for (int user: otherUsers) {
                    if (user != currentTrackingUserId) {
                        currentTrackingUserId = user;
                        break;
                    }
                }
                trackingUserChanged(preId, currentTrackingUserId);
            } else {
                currentTrackingUserId = -1;
                trackingUserChanged(preId, currentTrackingUserId);
            }

        }
    }

    BlobDetection getBlobDetection() {
        return blobDetection;
    }

    float getScale() {
        return scale;
    }

    float getOffset() {
        return offset;
    }
}
