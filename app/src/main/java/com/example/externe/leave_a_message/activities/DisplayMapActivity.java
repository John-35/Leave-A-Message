package com.example.externe.leave_a_message.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.example.externe.leave_a_message.R;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;

public class DisplayMapActivity extends AppCompatActivity implements LocationListener {

    private static final String TAG = "DisplayMapActivity";

    public static final String INTENT_LOCATION = "location";

    private final long MIN_TIME = 500; //milliseconds
    private final float MIN_DIST = 1; //meters

    private LocationManager locationManager;

    private IMapController mapController;

    private Location mLocation = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DIST, this);

        boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!enabled) {
        }

        MapView mapView = (MapView) findViewById(R.id.map);
        //mapView.setTileSource(TileSourceFactory.MAPNIK);


        /*
        public XYTileSource(final String aName, final int aZoomMinLevel,
			final int aZoomMaxLevel, final int aTileSizePixels, final String aImageFilenameEnding,
			final String[] aBaseUrl)
         */
        String[] urls = {"http://tile.stamen.com/toner/"}; //"http://tile.stamen.com/watercolor/"
        ITileSource tileSource = new XYTileSource("stamen", 1, 20, 256, ".png", urls);
        mapView.setTileSource(tileSource);


        mapController = mapView.getController();
        mapController.setZoom(30);
        GeoPoint startPoint = new GeoPoint(48.8583, 2.2944);
        mapController.setCenter(startPoint);

        RotationGestureOverlay mRotationGestureOverlay = new RotationGestureOverlay(this, mapView);
        mRotationGestureOverlay.setEnabled(true);
        mapView.setMultiTouchControls(true);
        mapView.getOverlays().add(mRotationGestureOverlay);

    }

    private void downloadMessages() {
        Toast.makeText(this, "Download", Toast.LENGTH_SHORT).show();
        String url = "";
        Ion.with(this)
        .load("http://jonathanperrinet.fr/experimental/leaveamessage/getmessage")
        .setMultipartParameter("lat", String.valueOf(mLocation.getLatitude()))
        .setMultipartParameter("lng", String.valueOf(mLocation.getLongitude()))
        .asString()
        .setCallback(new FutureCallback<String>() {
            @Override
            public void onCompleted(Exception e, String s) {
                Toast.makeText(DisplayMapActivity.this, "Result: " + s, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        GeoPoint position = new GeoPoint(location.getLatitude(), location.getLongitude());
        mapController.setCenter(position);

        mLocation = location;

        downloadMessages();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, "Enabled new provider " + provider, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Disabled provider " + provider, Toast.LENGTH_SHORT).show();
    }

    public void onClickFabBtn(View view) {
        Intent intent = new Intent(this, DrawActivity.class);
        intent.putExtra(INTENT_LOCATION, mLocation);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DIST, this);
        } catch (SecurityException se) {
            se.printStackTrace();
        }

    }

    /* Remove the locationlistener updates when Activity is paused */
    @Override
    protected void onPause() {
        super.onPause();

        try {
            locationManager.removeUpdates(this);
        } catch (SecurityException se) {
            se.printStackTrace();
        }
    }
}
