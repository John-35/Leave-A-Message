package fr.jonathanperrinet.leave_a_message.utils;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * Created by Jonathan Perrinet on 02/08/2016.
 */
public class CameraView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "CameraView";
    private SurfaceHolder holder;
    private Camera camera;

    public CameraView(Context context, Camera camera) {
        super(context);

        this.camera = camera;
        camera.setDisplayOrientation(90);

        holder = getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_NORMAL);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        try {
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        } catch (IOException e) {
            Log.e(TAG, "Camera error on surfaceCreated " + e.getMessage());
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        //before changing the application orientation, you need to stop the preview, rotate and then start it again
        if(holder.getSurface() == null)//check if the surface is ready to receive camera data
            return;

        try{
            camera.stopPreview();
        } catch (Exception e){
            //this will happen when you are trying the camera if it's not running
        }

        //now, recreate the camera preview
        try{
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Camera error on surfaceChanged " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        //our app has only one screen, so we'll destroy the camera in the surface
        //if you are unsing with more screens, please move this code your activity
        camera.stopPreview();
        camera.release();
    }
}
