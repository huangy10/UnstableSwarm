import blobDetection.Blob;
import blobDetection.BlobDetection;
import blobDetection.EdgeVertex;
import processing.core.PVector;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;

class PolygonBlob extends Polygon {

    KinectWrapper kinect;

    PolygonBlob(KinectWrapper kinect) {
        super();
        this.kinect = kinect;
    }

    // took me some time to make this method fully self-sufficient
    // now it works quite well in creating a correct polygon from a person's blob
    // of course many thanks to v3ga, because the library already does a lot of the work
    void createPolygon() {
        // an arrayList... of arrayLists... of PVectors
        // the arrayLists of PVectors are basically the person's contours (almost but not completely in a polygon-correct order)
        ArrayList contours = new ArrayList();
        // helpful variables to keep track of the selected contour and point (start/end point)
        int selectedContour = 0;
        int selectedPoint = 0;

        BlobDetection theBlobDetection = kinect.getBlobDetection();
        Sketch sk = Sketch.getSK();

        // create contours from blobs
        // go over all the detected blobs
        for (int n=0; n < theBlobDetection.getBlobNb (); n++) {
            Blob b = theBlobDetection.getBlob (n);
            // for each substantial blob...
            if (b != null && b.getEdgeNb() > 100) {
                // create a new contour arrayList of PVectors
                ArrayList contour = new ArrayList();
                // go over all the edges in the blob
                for (int m=0; m < b.getEdgeNb (); m++) { // get the edgeVertices of the edge
                    EdgeVertex eA = b.getEdgeVertexA(m);
                    EdgeVertex eB = b.getEdgeVertexB(m);
                    // if both ain't null...
                    if (eA != null && eB != null) {
                        // get next and previous edgeVertexA
                        EdgeVertex fn = b.getEdgeVertexA((m+1) % b.getEdgeNb());
                        EdgeVertex fp = b.getEdgeVertexA((Math.max(0, m-1)));
                        // calculate distance between vertexA and next and previous edgeVertexA respectively
                        // positions are multiplied by kinect dimensions because the blob library returns normalized values
                        float dn = Sketch.dist(
                                eA.x*KinectWrapper.kinectWidth, eA.y*KinectWrapper.kinectHeight,
                                fn.x*KinectWrapper.kinectWidth, fn.y*KinectWrapper.kinectHeight);
                        float dp = Sketch.dist(
                                eA.x*KinectWrapper.kinectWidth, eA.y*KinectWrapper.kinectHeight,
                                fp.x*KinectWrapper.kinectWidth, fp.y*KinectWrapper.kinectHeight);
                        // if either distance is bigger than 15
                        if (dn > 15 || dp > 15) {
                            // if the current contour size is bigger than zero
                            if (contour.size() > 0) {
                                // add final point
                                contour.add(new PVector(eB.x*KinectWrapper.kinectWidth,
                                        eB.y*KinectWrapper.kinectHeight));
                                // add current contour to the arrayList
                                contours.add(contour);
                                // start a new contour arrayList
                                contour = new ArrayList();
                                // if the current contour size is 0 (aka it's a new list)
                            } else {
                                // add the point to the list
                                contour.add(new PVector(eA.x*KinectWrapper.kinectWidth,
                                        eA.y*KinectWrapper.kinectHeight));
                            }
                            // if both distance are smaller than 15 (aka the points are close)
                        } else {
                            // add the point to the list
                            contour.add(new PVector(eA.x*KinectWrapper.kinectWidth,
                                    eA.y*KinectWrapper.kinectHeight));
                        }
                    }
                }
            }
        }

        // at this point in the code we have a list of contours (aka an arrayList of arrayLists of PVectors)
        // now we need to sort those contours into a correct polygon. To do this we need two things:
        // 1. The correct order of contours
        // 2. The correct direction of each contour

        // as long as there are contours left...
        while (contours.size () > 0) {

            // find next contour
            float distance = 999999999;
            // if there are already points in the polygon
            if (npoints > 0) {
                // use the polygon's last point as a starting point
                PVector lastPoint = new PVector(xpoints[npoints-1], ypoints[npoints-1]);
                // go over all contours
                for (int i=0; i < contours.size (); i++) {
                    ArrayList c = (ArrayList)contours.get(i);
                    // get the contour's first point
                    PVector fp = (PVector)c.get(0);
                    // get the contour's last point
                    PVector lp = (PVector)c.get(c.size()-1);
                    // if the distance between the current contour's first point and the polygon's last point is smaller than distance
                    if (fp.dist(lastPoint) < distance) {
                        // set distance to this distance
                        distance = fp.dist(lastPoint);
                        // set this as the selected contour
                        selectedContour = i;
                        // set selectedPoint to 0 (which signals first point)
                        selectedPoint = 0;
                    }
                    // if the distance between the current contour's last point and the polygon's last point is smaller than distance
                    if (lp.dist(lastPoint) < distance) {
                        // set distance to this distance
                        distance = lp.dist(lastPoint);
                        // set this as the selected contour
                        selectedContour = i;
                        // set selectedPoint to 1 (which signals last point)
                        selectedPoint = 1;
                    }
                }
                // if the polygon is still empty
            } else {
                // use a starting point in the lower-right
                PVector closestPoint = new PVector(sk.width, sk.height);
                // go over all contours
                for (int i=0; i < contours.size (); i++) {
                    ArrayList c = (ArrayList)contours.get (i);
                    // get the contour's first point
                    PVector fp = (PVector)c.get(0);
                    // get the contour's last point
                    PVector lp = (PVector)c.get(c.size()-1);
                    // if the first point is in the lowest 5 pixels of the (kinect) screen and more to the left than the current closestPoint
                    if (fp.y > KinectWrapper.kinectHeight-5 && fp.x < closestPoint.x) {
                        // set closestPoint to first point
                        closestPoint = fp;
                        // set this as the selected contour
                        selectedContour = i;
                        // set selectedPoint to 0 (which signals first point)
                        selectedPoint = 0;
                    }
                    // if the last point is in the lowest 5 pixels of the (kinect) screen and more to the left than the current closestPoint
                    if (lp.y > KinectWrapper.kinectHeight-5 && lp.x < closestPoint.y) {
                        // set closestPoint to last point
                        closestPoint = lp;
                        // set this as the selected contour
                        selectedContour = i;
                        // set selectedPoint to 1 (which signals last point)
                        selectedPoint = 1;
                    }
                }
            }
            // add contour to polygon
            ArrayList contour = (ArrayList)contours.get(selectedContour);
            // if selectedPoint is bigger than zero (aka last point) then reverse the arrayList of points
            if (selectedPoint > 0) {
                Collections.reverse(contour);
            }
            // add all the points in the contour to the polygon

            for( int i=0; i<contour.size(); i++){
                PVector p = (PVector)contour.get(i);
                addPoint((int)(p.x), (int)(p.y));
            }
            // remove this contour from the list of contours
            contours.remove(selectedContour);
            // the while loop above makes all of this code loop until the number of contours is zero
            // at that time all the points in all the contours have been added to the polygon... in the correct order (hopefully)
        }
    }

    void drawPolygon() {
        if (npoints == 0) {
            return;
        }
        Sketch sk = Sketch.getSK();
        sk.pushMatrix();
        sk.stroke(255, 0, 0);
        sk.noFill();
        sk.strokeWeight(4);
        sk.translate(kinect.getOffset(), 0);
        sk.scale(kinect.getScale());
        sk.beginShape();
        for (int i = 0; i < npoints; i += 1) {
            sk.vertex(xpoints[i], ypoints[i]);
        }
        sk.endShape(sk.CLOSE);
        sk.popMatrix();
    }

    boolean contains(PVector pt) {
        return super.contains((pt.x - kinect.getOffset()) / kinect.getScale(), pt.y / kinect.getScale());
    }

    void coordConvertToScreen(float x, float y, PVector pt) {
        pt.x = x * kinect.getScale() + kinect.getOffset();
        pt.y = y * kinect.getScale();
    }
}