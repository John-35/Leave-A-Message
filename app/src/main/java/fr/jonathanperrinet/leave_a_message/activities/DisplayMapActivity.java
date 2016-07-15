package fr.jonathanperrinet.leave_a_message.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.async.parser.JSONArrayParser;
import com.koushikdutta.ion.Ion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.api.IMapController;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ItemizedIconOverlay;
import org.osmdroid.views.overlay.ItemizedOverlayWithFocus;
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;

import java.util.ArrayList;

import fr.jonathanperrinet.leave_a_message.leave_a_message.R;

public class DisplayMapActivity extends AppCompatActivity implements LocationListener {

    private static final String TAG = "DisplayMapActivity";

    public static final String INTENT_LOCATION = "location";
    public static final String TILE_URL = "http://tile.stamen.com/toner/";  //"http://tile.stamen.com/watercolor/"
    public static final String GETMESSAGE_URL = "http://jonathanperrinet.fr/experimental/leaveamessage/getmessage";

    private final long MIN_TIME = 500; //milliseconds
    private final float MIN_DIST = 1; //meters

    private LocationManager locationManager;

    private IMapController mapController;

    private Location mLocation = null;

    ArrayList<OverlayItem> items;
    ItemizedOverlayWithFocus<OverlayItem> mItemOverlay;

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
        String[] urls = {TILE_URL};
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

        items = new ArrayList<>();
        mItemOverlay = new ItemizedOverlayWithFocus<OverlayItem>(items, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
            @Override
            public boolean onItemSingleTapUp(int index, OverlayItem item) {
                Toast.makeText(DisplayMapActivity.this, item.getTitle(), Toast.LENGTH_SHORT).show();
                return true;
            }

            @Override
            public boolean onItemLongPress(int index, OverlayItem item) {
                Toast.makeText(DisplayMapActivity.this, "onItemLongPress", Toast.LENGTH_SHORT).show();
                return true;
            }
        }, getApplicationContext());
        mapView.getOverlays().add(mItemOverlay);

        mapView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                downloadMessages();
                return false;
            }
        });
    }

    private void downloadMessages() {
        Toast.makeText(this, "Download", Toast.LENGTH_SHORT).show();
        String url = "";
        Ion.with(this)
        .load(GETMESSAGE_URL)
        .setMultipartParameter("lat", String.valueOf(mLocation.getLatitude()))
        .setMultipartParameter("lng", String.valueOf(mLocation.getLongitude()))
        .asString()
        .setCallback(new FutureCallback<String>() {
            @Override
            public void onCompleted(Exception e, String s) {
                Toast.makeText(DisplayMapActivity.this, "Result: " + s, Toast.LENGTH_SHORT).show();
                try {
                    JSONArray json = new JSONArray(s);
                    for(int i = 0 ; i < json.length() ; i++) {
                        JSONObject obj = json.getJSONObject(i);

                        //mItemOverlay.addItem(new OverlayItem("Test", "SampleDescription", position));
                    }
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
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
