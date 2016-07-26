package fr.jonathanperrinet.leave_a_message.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
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
import org.osmdroid.views.overlay.OverlayItem;
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay;
import org.osmdroid.views.overlay.mylocation.SimpleLocationOverlay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import fr.jonathanperrinet.leave_a_message.leave_a_message.R;
import fr.jonathanperrinet.leave_a_message.models.Message;
import fr.jonathanperrinet.leave_a_message.models.MessageString;
import fr.jonathanperrinet.leave_a_message.utils.App_Const;

public class DisplayMapActivity extends AppCompatActivity implements LocationListener {

    private static final String TAG = "DisplayMapActivity";

    public static final String INTENT_LOCATION_LAT = "location_lat";
    public static final String INTENT_LOCATION_LONG = "location_long";

    public static final String TILE_URL = "http://tile.stamen.com/toner/";  //"http://tile.stamen.com/watercolor/"
    private static final String SHARED_PREF_LAT = "last_lat";
    private static final String SHARED_PREF_LON = "last_lon";

    private final int REFRESH_TIME = 10000;

    private final long MIN_TIME = 500; //milliseconds
    private final float GPS_MIN_DIST = 1; //meters

    private final double MAX_DISTANCE = 0.5; // km
    private final int MAX_VIEWFIELD = 500; //meters

    private LocationManager locationManager;

    private IMapController mapController;

    private GeoPoint mGeoPointLocation = null;

    ArrayList<OverlayItem> items;
    ItemizedIconOverlay<OverlayItem> mItemOverlay;

    private SimpleLocationOverlay locationOverlay;

    HashMap<String, Message> messages = new HashMap<>();

    boolean firstTime = true;

    private MapView mapView;

    private Handler handlerUpdate;
    private Runnable runUpdateItemPresence;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);

        setContentView(R.layout.activity_map);

        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, GPS_MIN_DIST, this);

        boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!enabled) {
            //TODO: gestion position indisponible
        }

        mapView = (MapView) findViewById(R.id.map);
        String[] urls = {TILE_URL};
        final ITileSource tileSource = new XYTileSource("stamen", 1, 20, 256, ".png", urls);
        mapView.setTileSource(tileSource);

        //Load last position known
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        double lastLattitude = sharedPref.getFloat(SHARED_PREF_LAT, 48.8583f);
        double lastLongitude = sharedPref.getFloat(SHARED_PREF_LON, 2.2944f);

        mapController = mapView.getController();
        mapController.setZoom(30);
        GeoPoint startPoint = new GeoPoint(lastLattitude, lastLongitude);
        mapController.setCenter(startPoint);

        RotationGestureOverlay mRotationGestureOverlay = new RotationGestureOverlay(this, mapView);
        mRotationGestureOverlay.setEnabled(true);
        mapView.setMultiTouchControls(true);
        mapView.getOverlays().add(mRotationGestureOverlay);

        locationOverlay = new SimpleLocationOverlay(this);
        mapView.getOverlays().add(locationOverlay);


        items = new ArrayList<>();
        mItemOverlay = new ItemizedIconOverlay<>(items, new ItemizedIconOverlay.OnItemGestureListener<OverlayItem>() {
            @Override
            public boolean onItemSingleTapUp(int index, OverlayItem item) {
                MessageString msg = (MessageString)(messages.get(item.getTitle()));
                //Toast.makeText(DisplayMapActivity.this, msg.getMessage(), Toast.LENGTH_SHORT).show();
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DisplayMapActivity.this);
                alertDialogBuilder.setMessage(msg.getMessage())
                                  .setCancelable(true)
                                  .show();

                return true;
            }

            @Override
            public boolean onItemLongPress(int index, OverlayItem item) {
                Toast.makeText(DisplayMapActivity.this, "onItemLongPress", Toast.LENGTH_SHORT).show();
                return true;
            }
        }, getApplicationContext());
        mapView.getOverlays().add(mItemOverlay);

        /*
            handlerUpdate launch Runnable runUpdateItemPresence each REFRESH_TIME milliseconds.
            It checks if it exists items at a distance > MAX_VIEWFIELD and removes them if true
         */
        handlerUpdate = new Handler();
        runUpdateItemPresence = new Runnable() {
            @Override
            public void run() {
                if(mGeoPointLocation != null) {
                    GeoPoint itemGeoPoint;
                    Iterator<OverlayItem> it = items.iterator();
                    while (it.hasNext()) {
                        OverlayItem item = it.next();
                        itemGeoPoint = (GeoPoint) item.getPoint();
                        int distance = mGeoPointLocation.distanceTo(itemGeoPoint);
                        if (distance > MAX_VIEWFIELD) {
                            messages.remove(item.getTitle());
                            it.remove();
                            mItemOverlay.removeItem(item);
                        } else {
                            if (!messages.get(item.getTitle()).isLoaded()) {
                                openMessageFromServer(messages.get(item.getTitle()));
                            }
                        }
                    }
                    mapView.invalidate(); //update the mapview display
                    downloadMessages(mGeoPointLocation);
                }
                handlerUpdate.postDelayed(this, REFRESH_TIME);
            }
        };
        handlerUpdate.postDelayed(runUpdateItemPresence, REFRESH_TIME);
    }

    private void downloadMessages(GeoPoint location) {
        //Toast.makeText(this, "Download around " + location.getLatitude() + " ; " + location.getLongitude(), Toast.LENGTH_LONG).show();
        Ion.with(this)
                .load(App_Const.URL_LIST)
                .setMultipartParameter("lat", String.valueOf(location.getLatitude()))
                .setMultipartParameter("lng", String.valueOf(location.getLongitude()))
                .setMultipartParameter("dist", String.valueOf(MAX_DISTANCE))
                .asJsonArray()
                .setCallback(new FutureCallback<com.google.gson.JsonArray>() {
                    @Override
                    public void onCompleted(Exception e, com.google.gson.JsonArray jsonArray) {
                        if(jsonArray != null) {
                            Iterator<JsonElement> iterator = jsonArray.iterator();
                            JsonObject jsonObject;
                            while(iterator.hasNext()) {
                                jsonObject = iterator.next().getAsJsonObject();
                                //Toast.makeText(DisplayMapActivity.this, "obj: " + jsonObject, Toast.LENGTH_SHORT).show();
                                double latitude = jsonObject.get("lat").getAsDouble();
                                double longitude = jsonObject.get("lng").getAsDouble();
                                String url = jsonObject.get("url").getAsString();
                                if(!messages.containsKey(url)) {
                                    Message msg = new MessageString(url, latitude, longitude, 0, 0, 0, null);
                                    messages.put(url, msg);
                                    mItemOverlay.addItem(new OverlayItem(url, "SampleDescription", new GeoPoint(latitude, longitude)));
                                    openMessageFromServer(msg);
                                }
                            }
                        }
                        mapView.invalidate();
                    }
                });
    }

    private void openMessageFromServer(final Message msg) {
        /*Ion.with(this)
                .load(App_Const.URL_GET_MESSAGE)
                .setBodyParameter("url", url)
                .asJsonObject()
                .setCallback(new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject jsonObj) {
                        if(jsonObj != null) {
                            Toast.makeText(DisplayMapActivity.this, url + " : " + jsonObj.getAsString(), Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(DisplayMapActivity.this, url + " : " + jsonObj, Toast.LENGTH_SHORT).show();
                        }
                    }
                });*/

        Ion.with(this)
                .load(App_Const.URL_GET_MESSAGE)
                .setMultipartParameter("url", msg.getUrl())
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String json) {
                        /*if(json != null) {
                            try {
                                JSONObject jsonObj = new JSONObject(json);
                                ((MessageString)msg).setMessage(jsonObj.toString());
                                Toast.makeText(DisplayMapActivity.this, json, Toast.LENGTH_SHORT).show();
                            } catch (JSONException jse) {
                                Log.e(TAG, jse.getMessage());
                            }
                            //Toast.makeText(DisplayMapActivity.this, url + " : " + jsonObj, Toast.LENGTH_SHORT).show();
                        } else {
                            //Toast.makeText(DisplayMapActivity.this, url + " : " + jsonObj, Toast.LENGTH_SHORT).show();
                        }*/
                        if(json != null) {
                            ((MessageString) msg).setMessage(json);
                            msg.setLoaded(true);
                        } else {
                            ((MessageString) msg).setMessage(getResources().getString(R.string.erreur_loading_message));
                        }
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

        mGeoPointLocation = new GeoPoint(location.getLatitude(), location.getLongitude());
        locationOverlay.setLocation(mGeoPointLocation);
        downloadMessages(mGeoPointLocation);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        //Toast.makeText(this, "Enabled new provider " + provider, Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onProviderDisabled(String provider) {
        //Toast.makeText(this, "Disabled provider " + provider, Toast.LENGTH_SHORT).show();
    }

    /*
        Starts the draw activity when the user clicks on the floating button
     */
    public void onClickFabBtn(View view) {
        if(mGeoPointLocation != null) {
            Intent intent = new Intent(this, DrawActivity.class);
            intent.putExtra(INTENT_LOCATION_LAT, mGeoPointLocation.getLatitude());
            intent.putExtra(INTENT_LOCATION_LONG, mGeoPointLocation.getLongitude());
            startActivity(intent);
        } else {
            Toast.makeText(this, "Your position is not set !", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        Ion.getDefault(this).cancelAll();
        handlerUpdate.postDelayed(runUpdateItemPresence, REFRESH_TIME);

        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, GPS_MIN_DIST, this);
        } catch (SecurityException se) {
            se.printStackTrace();
        }

    }

    /* Remove the locationlistener updates when Activity is paused */
    @Override
    protected void onPause() {
        super.onPause();

        //Load last position known
        if(mGeoPointLocation != null) {
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putFloat(SHARED_PREF_LAT, (float) mGeoPointLocation.getLatitude());
            editor.putFloat(SHARED_PREF_LON, (float) mGeoPointLocation.getLongitude());
            editor.commit();
        }

        handlerUpdate.removeCallbacks(runUpdateItemPresence);

        try {
            locationManager.removeUpdates(this);
        } catch (SecurityException se) {
            se.printStackTrace();
        }
    }

    public void onClickCenterPosition(View view) {
        if(mGeoPointLocation != null) {
            Toast.makeText(this, "Your are at position : " + mGeoPointLocation, Toast.LENGTH_SHORT).show();
            mapController.animateTo(mGeoPointLocation);
        } else {
            Toast.makeText(this, "Unknown position", Toast.LENGTH_SHORT).show();
        }
    }
}
