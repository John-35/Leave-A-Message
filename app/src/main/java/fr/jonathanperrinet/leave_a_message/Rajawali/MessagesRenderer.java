package fr.jonathanperrinet.leave_a_message.Rajawali;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;

import org.rajawali3d.Object3D;
import org.rajawali3d.cameras.Camera;
import org.rajawali3d.curves.CompoundCurve3D;
import org.rajawali3d.curves.CubicBezierCurve3D;
import org.rajawali3d.curves.ICurve3D;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Line3D;
import org.rajawali3d.primitives.Sphere;
import org.rajawali3d.renderer.RajawaliRenderer;

import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import fr.jonathanperrinet.leave_a_message.model.BezierCurve;
import fr.jonathanperrinet.leave_a_message.model.Message;
import fr.jonathanperrinet.leave_a_message.model.MessageDrawn;
import fr.jonathanperrinet.leave_a_message.model.ParcelableVector3;

/**
 * Created by Jonathan Perrinet on 02/08/2016.
 */
public class MessagesRenderer extends RajawaliRenderer {

    private static final String TAG = "MessagesRenderer";
    private Context context;
    private DirectionalLight directionalLight;

    float xStartPos, xpos;
    double flickStart;

    Camera cam;
    Quaternion quatCameraInit;

    double angle = 0;

    final Material material = new Material();

    RendererListener listener = null;

    public interface RendererListener {
        HashMap<String, Message> getMessages();
    }

    public MessagesRenderer(Context context) {
        super(context);
        this.context = context;
        this.listener = (RendererListener)context;
        setFrameRate(60);
        //Toast.makeText(context, "Listener: " + listener, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void initScene() {
        directionalLight = new DirectionalLight(1f, .2f, -1.0f);
        directionalLight.setColor(1.0f, 1.0f, 1.0f);
        directionalLight.setPower(2);
        getCurrentScene().addLight(directionalLight);

        material.setColor(0xffffff);

        Sphere sphere = new Sphere(20, 10, 10);
        sphere.setMaterial(material);
        getCurrentScene().addChild(sphere);

        cam = getCurrentCamera();
        cam.setFarPlane(2000);
        cam.setY(75);
        cam.setZ(200);
        cam.setX(50);

        quatCameraInit = getCurrentCamera().getOrientation();
        Log.i(TAG, "Quat ini: " + quatCameraInit);

        createMessageObjects();
    }

    @Override
    public void onOffsetsChanged(float xOffset, float yOffset, float xOffsetStep, float yOffsetStep, int xPixelOffset, int yPixelOffset) {

    }

    @Override
    public void onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                Log.d(TAG, "ACTION_DOWN");
                xStartPos = event.getX();
                xpos = xStartPos;
                flickStart = getCurrentCamera().getZ();
                break;

            case MotionEvent.ACTION_MOVE:
                Log.d(TAG, "ACTION_MOVE");
                float xd = xpos - event.getX();
                //swipeCamera(xd);
                xpos = event.getX();
                break;

            case MotionEvent.ACTION_UP:
                Log.d(TAG, "ACTION_UP");
                float xEndPos = event.getX();
                float delta = xEndPos - xStartPos;
                //flickrCamera(delta);
                break;
        }
    }

    public void createMessageObjects() {
        if(listener != null) {
            HashMap<String, Message> messages = listener.getMessages();
            if(messages != null) {
                for(String key : messages.keySet()) {
                    Message msg = messages.get(key);
                    if(msg.isLoaded()) {
                        if(msg instanceof MessageDrawn) {
                            addBezier(key, ((MessageDrawn) msg).getCurves());
                        }
                    }

                }
            }
        }
    }

    /*@Override
    public void onRender(final long elapsedTime, final double deltaTime) {
        super.onRender(elapsedTime, deltaTime);
        angle = (angle + 0.5) % 360;
        cam.rotate(Vector3.Axis.Y, map(angle, 0, 359, -Math.PI, Math.PI));
    }

    private double map(double value, int aFrom, int aTo, double bFrom, double bTo) {
        return (value - aFrom) / (aTo - aFrom) * (bTo - bFrom) + bFrom;
    }*/

    private void addBezier(String name, List<BezierCurve> curves) {
        final int SCALE = 100;
        Log.i(TAG, "addBezier: " + curves);

        if(curves != null) {
            Object3D obj = new Object3D(name);
            CompoundCurve3D compound = new CompoundCurve3D();
            ParcelableVector3 lastPoint = null;

            for(int i = 0 ; i < curves.size() ; i++) {
                BezierCurve curve = curves.get(i);
                CubicBezierCurve3D cubicBezierCurve = new CubicBezierCurve3D(new Vector3(curve.startPoint.x * SCALE, (1 - curve.startPoint.y) * SCALE, curve.startPoint.z),
                        new Vector3(curve.control1.x * SCALE, (1 - curve.control1.y) * SCALE, curve.control1.z),
                        new Vector3(curve.control2.x * SCALE, (1 - curve.control2.y) * SCALE, curve.control2.z),
                        new Vector3(curve.endPoint.x * SCALE, (1 - curve.endPoint.y) * SCALE, curve.endPoint.z));

                if(lastPoint != null && !curve.startPoint.sameAs(lastPoint)) {
                    obj.addChild(computeCurve(compound));
                    compound = new CompoundCurve3D();
                }

                compound.addCurve(cubicBezierCurve);
                lastPoint = curve.endPoint;
            }

            if(compound.getNumCurves() > 0) {
                obj.addChild(computeCurve(compound));
            }

            getCurrentScene().addChild(obj);
        }
    }

    private Line3D computeCurve(ICurve3D curve) {
        Stack<Vector3> points = new Stack<>();
        int subdiv = 100;
        for (int j = 0; j <= subdiv; j++) {
            Vector3 point = new Vector3();
            curve.calculatePoint(point, (float) j / (float) subdiv);
            points.push(point);
        }

        Line3D line = new Line3D(points, 10);
        line.setMaterial(material);

        return line;
    }

    public void updateCameraOrientation(float roll, float pitch, float bearing) {
        Quaternion quatPitch = new Quaternion(new Vector3(1, 0, 0), Math.toDegrees(pitch));
        Quaternion quatBearing = new Quaternion(new Vector3(0, 1, 0), Math.toDegrees(bearing));
        Quaternion quatRoll = new Quaternion(new Vector3(0, 0, 1), Math.toDegrees(roll + Math.PI / 2));

        Quaternion quatRotateWith = quatBearing.multiply(quatRoll.multiply(quatPitch));
        //getCurrentCamera().setCameraYaw(Math.toDegrees(bearing));
        //getCurrentCamera().setCameraRoll(Math.toDegrees(roll));
        //getCurrentCamera().setCameraPitch(Math.toDegrees(pitch));

        if(quatCameraInit != null && quatRotateWith != null) {
            quatCameraInit.slerp(quatBearing, 0.1);
            getCurrentCamera().setCameraOrientation(quatRotateWith);
        }
    }

}
