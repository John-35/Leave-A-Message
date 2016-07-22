package fr.jonathanperrinet.leave_a_message.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

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
import java.util.HashMap;
import java.util.Iterator;

import fr.jonathanperrinet.leave_a_message.leave_a_message.R;
import fr.jonathanperrinet.leave_a_message.models.Message;
import fr.jonathanperrinet.leave_a_message.models.MessageString;
import fr.jonathanperrinet.leave_a_message.utils.App_Const;

public class DisplayMapActivity extends AppCompatActivity implements LocationListener {

    private static final String TAG = "DisplayMapActivity";

    public static final String INTENT_LOCATION = "location";
    public static final String TILE_URL = "http://tile.stamen.com/toner/";  //"http://tile.stamen.com/watercolor/"

    private final long MIN_TIME = 500; //milliseconds
    private final float MIN_DIST = 1; //meters

    private LocationManager locationManager;

    private IMapController mapController;

    private Location mLocation = null;

    ArrayList<OverlayItem> items;
    ItemizedOverlayWithFocus<OverlayItem> mItemOverlay;

    HashMap<String, Message> messages = new HashMap<>();

    boolean firstTime = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, MIN_DIST, this);

        boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!enabled) {
            //TODO: gestion position indisponible
        }

        MapView mapView = (MapView) findViewById(R.id.map);
        String[] urls = {TILE_URL};
        ITileSource tileSource = new XYTileSource("stamen", 1, 20, 256, ".png", urls);
        try {
            mapView.setTileSource(tileSource);
        } catch(NullPointerException e) {
            Log.e(TAG, e.getMessage());
        }


        mapController = mapView.getController();
        mapController.setZoom(30);
        GeoPoint startPoint = new GeoPoint(48.8583, 2.2944);
        mapController.setCenter(startPoint);

        RotationGestureOverlay mRotationGestureOverlay = new RotationGestureOverlay(this, mapView);
        mRotationGestureOverlay.setEnabled(true);
        mapView.setMultiTouchControls(true);
        mapView.getOverlays().add(mRotationGestureOverlay);

        items = new ArrayList<>();
        mItemOverlay = new ItemizedOverlayWithFocus<>(items, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
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
        //Toast.makeText(this, "Download", Toast.LENGTH_SHORT).show();
        Ion.with(this)
                .load(App_Const.URL_LIST)
                .setMultipartParameter("lat", String.valueOf(48.107175536209155))
                .setMultipartParameter("lng", String.valueOf(-1.693147445715343))
                .setMultipartParameter("dist", String.valueOf(10))
                .asJsonArray()
                .setCallback(new FutureCallback<com.google.gson.JsonArray>() {
                    @Override
                    public void onCompleted(Exception e, com.google.gson.JsonArray jsonArray) {
                        Log.d(TAG, "jsonArray: " + jsonArray);
                        Iterator<JsonElement> iterator = jsonArray.iterator();
                        JsonObject jsonObject;
                        /*while(iterator.hasNext()) {
                            jsonObject = iterator.next().getAsJsonObject();
                            Log.d(TAG, "jsonObject: " + jsonObject);
                            double latitude = jsonObject.get("lat").getAsDouble();
                            double longitude = jsonObject.get("lng").getAsDouble();
                            String url = jsonObject.get("url").getAsString();
                            if(!messages.containsKey(url)) {
                                messages.put(url, new MessageString(latitude, longitude, 0, 0, 0, url));
                                mItemOverlay.addItem(new OverlayItem(url, "SampleDescription", new GeoPoint(latitude, longitude)));
                                openMessageFromServer(url);
                            }
                        }*/
                    }
                });
    }

    private void openMessageFromServer(String url) {
        Toast.makeText(DisplayMapActivity.this, "Opening: " + url, Toast.LENGTH_SHORT).show();
        Ion.with(this)
                .load(App_Const.URL_GET_MESSAGE)
                .setBodyParameter("url", url)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject jsonObj) {
                        Toast.makeText(DisplayMapActivity.this, "Content: " + jsonObj.getAsString(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    public void onLocationChanged(Location location) {
        GeoPoint position = new GeoPoint(location.getLatitude(), location.getLongitude());
        if(firstTime) {
            mapController.setCenter(position);
            firstTime = false;
        }

        mLocation = location;

        Ion.getDefault(this).cancelAll();
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
