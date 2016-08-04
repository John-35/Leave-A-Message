package fr.jonathanperrinet.leave_a_message.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import org.rajawali3d.surface.IRajawaliSurface;
import org.rajawali3d.surface.RajawaliSurfaceView;

import java.util.ArrayList;
import java.util.List;

import fr.jonathanperrinet.leave_a_message.Rajawali.MessagesRenderer;
import fr.jonathanperrinet.leave_a_message.leave_a_message.R;
import fr.jonathanperrinet.leave_a_message.model.BezierCurve;
import fr.jonathanperrinet.leave_a_message.utils.CameraView;

/**
 * Created by Jonathan Perrinet.
 */
public class AugmentedViewActivity extends AppCompatActivity implements MessagesRenderer.RendererListener {

    private static final String TAG = "Activity3D";
    MessagesRenderer renderer;

    private CameraView cameraView = null;
    private Camera camera = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_augmented_view);

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
    public List<String> getMessages() {
        List<String> messages = new ArrayList<>();

        Intent intent = getIntent();
        if(intent != null) {
            String svg = intent.getStringExtra("svg");
            messages.add(svg);
        }

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

    }
}
