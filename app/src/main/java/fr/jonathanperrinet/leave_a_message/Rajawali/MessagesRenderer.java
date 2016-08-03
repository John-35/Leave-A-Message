package fr.jonathanperrinet.leave_a_message.Rajawali;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;

import org.rajawali3d.cameras.Camera;
import org.rajawali3d.curves.CompoundCurve3D;
import org.rajawali3d.curves.ICurve3D;
import org.rajawali3d.curves.SVGPath;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Line3D;
import org.rajawali3d.primitives.Sphere;
import org.rajawali3d.renderer.RajawaliRenderer;

import java.util.List;
import java.util.Stack;

/**
 * Created by Jonathan Perrinet on 02/08/2016.
 */
public class MessagesRenderer extends RajawaliRenderer {

    private static final String TAG = "MessagesRenderer";
    private Context context;
    private DirectionalLight directionalLight;
    private Sphere sphere;

    float xStartPos, xpos;
    double flickStart;

    Camera cam;

    double angle = 0;

    RendererListener listener = null;

    public interface RendererListener {
        public List<String> getMessages();
    }

    public MessagesRenderer(Context context) {
        super(context);
        this.context = context;
        this.listener = (RendererListener)context;
        setFrameRate(60);
    }

    @Override
    protected void initScene() {
        directionalLight = new DirectionalLight(1f, .2f, -1.0f);
        directionalLight.setColor(1.0f, 1.0f, 1.0f);
        directionalLight.setPower(2);
        getCurrentScene().addLight(directionalLight);

        Material material = new Material();
        material.enableLighting(true);
        material.setDiffuseMethod(new DiffuseMethod.Lambert());
        material.setColor(0xaa5500);

        sphere = new Sphere(1, 24, 24);
        sphere.setMaterial(material);
        getCurrentScene().addChild(sphere);

        cam = getCurrentCamera();
        cam.setFarPlane(2000);
        cam.setY(50);
        cam.setZ(400);

        if(listener != null) {
            List<String> messages = listener.getMessages();
            for (String msg : messages) {
                addSvg(msg);
            }
        }
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

    @Override
    public void onRender(final long elapsedTime, final double deltaTime) {
        super.onRender(elapsedTime, deltaTime);
        angle = (angle + 0.5) % 360;
        //cam.rotate(Vector3.Axis.Y, map(angle, 0, 359, -Math.PI, Math.PI));
    }

    private double map(double value, int aFrom, int aTo, double bFrom, double bTo) {
        return (value - aFrom) / (aTo - aFrom) * (bTo - bFrom) + bFrom;
    }

    private void addSvg(String path) {
        SVGPath svgPath = new SVGPath();
        Log.d(TAG, "Parse path " + path);
        /*path = "M22.395-127.223c-4.492,11.344-4.688,33.75,0,44.883"
                + "c-11.328-4.492-33.656-4.579-44.789,0.109c4.491-11.354,4.688-33.75,0-44.892"
                + "C-11.066-122.63,11.262-122.536,22.395-127.223z";*/

        path = "M42-39c134,149-134,149-223,39z";
        List<CompoundCurve3D> paths = svgPath.parseString(path);

        Material material = new Material();
        material.enableLighting(true);
        material.setDiffuseMethod(new DiffuseMethod.Lambert());
        material.setColor(0xaa5500);

        Log.d(TAG, "size: " + paths.size());
        for (int i = 0; i < paths.size(); i++) {
            ICurve3D subPath = paths.get(i);
            Stack<Vector3> points = new Stack<Vector3>();
            int subdiv = 1000;
            for (int j = 0; j <= subdiv; j++) {
                Vector3 point = new Vector3();
                subPath.calculatePoint(point, (float) j / (float) subdiv);
                points.add(point);
            }

            Line3D line = new Line3D(points, 10);
            line.setMaterial(material);
            getCurrentScene().addChild(line);
        }
    }
}
