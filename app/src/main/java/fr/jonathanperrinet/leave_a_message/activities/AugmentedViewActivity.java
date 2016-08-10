package fr.jonathanperrinet.leave_a_message.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.rajawali3d.surface.IRajawaliSurface;
import org.rajawali3d.surface.RajawaliSurfaceView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import fr.jonathanperrinet.leave_a_message.Rajawali.MessagesRenderer;
import fr.jonathanperrinet.leave_a_message.leave_a_message.R;
import fr.jonathanperrinet.leave_a_message.model.BezierCurve;
import fr.jonathanperrinet.leave_a_message.model.Message;
import fr.jonathanperrinet.leave_a_message.utils.CameraView;

/**
 * Created by Jonathan Perrinet.
 */
public class AugmentedViewActivity extends LocatedActivity implements MessagesRenderer.RendererListener, SensorEventListener {

    public static final String INTENT_ROT_X = "ax";
    public static final String INTENT_ROT_Y = "ay";
    public static final String INTENT_ROT_Z = "az";

    private static final String TAG = "AugmentedViewActivity";
    MessagesRenderer renderer;

    private CameraView cameraView = null;
    private Camera camera = null;

    private SensorManager sensorManager;

    private float[] accelerometerValues = new float[3];
    private float[] magneticFieldValues = new float[3];
    private final float[] rotationMatrix = new float[9];
    private final float[] orientationAngles = new float[3];

    private float bearing = 0.0f;
    private float pitch = 0.0f;
    private float roll = 0.0f;

    private float angle = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_augmented_view);

        Intent intent = getIntent();
        Log.i(TAG, "Intent: " + intent);
        if(intent != null) {
            Serializable value = intent.getSerializableExtra(INTENT_MESSAGES);
            Log.i(TAG, "Value intent: " + value);
            try {
                messages = (HashMap<String, Message>) value;
            } catch (ClassCastException cce) {
                Log.e(TAG, cce.getMessage());
            }
        }

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        //final RajawaliSurfaceView surface = (RajawaliSurfaceView)findViewById(R.id.rajawali_surface);
        final RajawaliSurfaceView surface = new RajawaliSurfaceView(this);
        surface.setFrameRate(60.0);
        surface.setRenderMode(IRajawaliSurface.RENDERMODE_WHEN_DIRTY);
        surface.setTransparent(true);
        surface.bringToFront();

        addContentView(surface, new ActionBar.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT));

        renderer = new MessagesRenderer(this);
        surface.setSurfaceRenderer(renderer);

        if(checkCameraHardware(this)) {
            try {
                camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
            } catch (Exception e) {
                Log.e(TAG, "Failed to get camera: " + e.getMessage());
            }

            if(camera != null) {
                cameraView = new CameraView(this, camera);//create a SurfaceView to show camera data
                FrameLayout camera_view_layout = (FrameLayout)findViewById(R.id.camera_view);
                camera_view_layout.addView(cameraView);//add the SurfaceView to the layout
            }
        } else {
            Toast.makeText(this, "Vous ne disposez pas de caméra.", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD), SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onMessageAdded(Message msg) {

    }

    @Override
    public void onMessageRemoved(Message msg) {

    }

    @Override
    public HashMap<String, Message> getMessages() {
        Log.i(TAG, "getMessages: " + messages);
        return messages;
    }

    @Override
    public List<BezierCurve> getCurves() {
        List<BezierCurve> curves = null;
        Intent intent = getIntent();
        if(intent != null) {
            curves = intent.getParcelableArrayListExtra("curves");
        }
        return curves;
    }

    public void onClickPlaceBtn(View view) {
        Intent intent = new Intent();
        intent.putExtra(INTENT_ROT_X, accelerometerValues[0]);
        intent.putExtra(INTENT_ROT_Y, accelerometerValues[1]);
        intent.putExtra(INTENT_ROT_Z, accelerometerValues[2]);
        setResult(DrawActivity.REQUEST_CODE, intent);
        finish();
    }

    public void onClickBtnEdit(View view) {
        finish();
    }

    /*@Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            accelerometerValues = applyLowPassFilter(event.values.clone(), accelerometerValues);
            //System.arraycopy(event.values, 0, accelerometerValues, 0, accelerometerValues.length);
        } else if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magneticFieldValues = applyLowPassFilter(event.values.clone(), magneticFieldValues);
            //System.arraycopy(event.values, 0, magnetometerValues, 0, magnetometerValues.length);
        }
    }*/

    @Override
    public void onSensorChanged(SensorEvent event) {
        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            //System.arraycopy(event.values, 0, accelerometerValues, 0, 3);
            accelerometerValues = applyLowPassFilter(event.values.clone(), accelerometerValues);
            /*Display display = ((WindowManager)getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
            int displayRotation = display.getRotation();

            float[] adjustedValues = new float[3];
            final int axisSwap[][] = {
                    {  1,  -1,  0,  1  },     // ROTATION_0
                    {-1,  -1,  1,  0  },     // ROTATION_90
                    {-1,    1,  0,  1  },     // ROTATION_180
                    {  1,    1,  1,  0  }  }; // ROTATION_270

            final int[] as = axisSwap[displayRotation];
            adjustedValues[0]  =  (float)as[0] * event.values[ as[2] ];
            adjustedValues[1]  =  (float)as[1] * event.values[ as[3] ];
            adjustedValues[2]  =  event.values[2];

            float x = adjustedValues[0];
            float y = adjustedValues[1];
            float z = adjustedValues[2];

            accelerometerValues[0] = x;
            accelerometerValues[1] = y;
            accelerometerValues[2] = z;*/
        } else if(event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD){
            //System.arraycopy(event.values, 0, magneticFieldValues, 0, 3);
            float[] newValues = new float[3];
            System.arraycopy(event.values, 0, newValues, 0, 3);
            magneticFieldValues = applyLowPassFilter(newValues, magneticFieldValues);
        }
        //updateOrientation();
    }

    private static final float ALPHA = 0.5f;
    private float[] applyLowPassFilter(float[] input, float[] output) {
        if(output == null) return input;

        for(int i = 0 ; i < input.length ; i++) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }

        return output;
    }

    protected void updateOrientation() {
        float[] values = new float[3];
        float[] inR = new float[9];
        float[] outR = new float[9];

        SensorManager.getRotationMatrix(inR, null, accelerometerValues, magneticFieldValues);
        SensorManager.remapCoordinateSystem(inR, SensorManager.AXIS_X, SensorManager.AXIS_Z, outR);
        SensorManager.getOrientation(outR, values);

        angle = angle + 0.5f;
        bearing = (float)Math.toRadians(angle);
        pitch = 0;
        roll = 0;

        Log.i(TAG, "bearing: " + Math.toDegrees(values[0]));

       /* bearing = values[0];
        pitch = values[1];
        roll = values[2];*/

        bearing = values[0];

        if(renderer != null) {
            renderer.updateCameraOrientation(roll, pitch, bearing);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
