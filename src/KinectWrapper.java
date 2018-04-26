import SimpleOpenNI.*;
import blobDetection.*;
import processing.core.PApplet;
import processing.core.PImage;


public class KinectWrapper {
    private SimpleOpenNI context;
    private BlobDetection blobDetection;
    private int currentTrackingUserId = -1;
    private boolean trackingUser;
    private float scale;
    static float kinectWidth = 640f;
    static float kinectHeight = 480f;
    private PImage blobProcessImage;
    private int step = 2;
    Sketch sk;
    PolygonBlob poly;
    private float offset = 0;

    KinectWrapper(PApplet applet) {
        context = new SimpleOpenNI(applet);
        sk = Sketch.getSK();
        trackingUser = false;
        poly = new PolygonBlob(this);
    }

    boolean configrueKinect() {
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

    void update() {
        context.update();
        int[] userMap = context.userMap();
        int idx, blobIdx;
        blobProcessImage.loadPixels();
        for (int i = 0; i < blobProcessImage.width; i += 1) {
            for (int j = 0; j < blobProcessImage.height; j += 1) {
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
    }

    void onNewUser(SimpleOpenNI curContext, int userId) {
        Sketch.println("OnNewUser: " + userId);
        currentTrackingUserId = userId;
        curContext.startTrackingSkeleton(userId);
    }

    void onLostUser(SimpleOpenNI curContext, int userId) {
        Sketch.println("OnLostUser: " + userId);
        if (trackingUser && userId == currentTrackingUserId) {
            trackingUser = false;
        }
    }

    void onVisibleUser(SimpleOpenNI curContext,int userId) {
        // do nothing for now
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
