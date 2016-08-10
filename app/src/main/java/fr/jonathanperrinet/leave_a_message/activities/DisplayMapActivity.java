package fr.jonathanperrinet.leave_a_message.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

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
import fr.jonathanperrinet.leave_a_message.model.Message;
import fr.jonathanperrinet.leave_a_message.utils.MessageManager;

/**
 * Created by Jonathan Perrinet.
 */
public class DisplayMapActivity extends LocatedActivity {

    private static final String TAG = "DisplayMapActivity";

    public static final String INTENT_LOCATION_LAT = "location_lat";
    public static final String INTENT_LOCATION_LONG = "location_long";

    public static final String TILE_URL = "http://tile.stamen.com/toner/";  //"http://tile.stamen.com/watercolor/"
    private static final String SHARED_PREF_LAT = "last_lat";
    private static final String SHARED_PREF_LON = "last_lon";

    private final int REFRESH_TIME = 10000;

    private IMapController mapController;

    ArrayList<OverlayItem> items;
    ItemizedIconOverlay<OverlayItem> mItemOverlay;

    private SimpleLocationOverlay locationOverlay;

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

        //StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());

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
                displayMessage(messages.get(item.getTitle()));
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
        //TODO: refactoriser cette méthode et la mettre dans LocatedActivity
        handlerUpdate = new Handler();
        runUpdateItemPresence = new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "run " + messages.keySet());

                if(myPosition != null) {
                    GeoPoint itemGeoPoint;
                    Iterator<OverlayItem> it = items.iterator();
                    while (it.hasNext()) {
                        OverlayItem item = it.next();
                        itemGeoPoint = (GeoPoint) item.getPoint();
                        int distance = myPosition.distanceTo(itemGeoPoint);
                        if (distance > MessageManager.MAX_DISTANCE * 1000) {
                            messages.remove(item.getTitle());
                            it.remove();
                            mItemOverlay.removeItem(item);
                        } else {
                            if (!messages.get(item.getTitle()).isLoaded()) {
                                MessageManager.openMessageFromServer(DisplayMapActivity.this, messages.get(item.getTitle()));
                            }
                        }
                    }
                    mapView.invalidate(); //update the mapview display
                    MessageManager.downloadMessages(DisplayMapActivity.this, myPosition);
                }
                handlerUpdate.postDelayed(this, REFRESH_TIME);
            }
        };
    }

    private void displayMessage(Message msg) {
        Log.i(TAG, "displayMessage " + msg);
        if(msg != null) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DisplayMapActivity.this);
            alertDialogBuilder.setMessage(msg.toString())
                    .setCancelable(true)
                    .show();
        }
    }

    /*
        Starts the draw activity when the user clicks on the floating button
     */
    public void onClickFabBtn(View view) {
        if(myPosition != null) {
            Intent intent = new Intent(this, DrawActivity.class);
            intent.putExtra(INTENT_LOCATION_LAT, myPosition.getLatitude());
            intent.putExtra(INTENT_LOCATION_LONG, myPosition.getLongitude());
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
    }

    @Override
    protected void onPause() {
        super.onPause();
        handlerUpdate.removeCallbacks(runUpdateItemPresence);

        //TODO: enregistrer la liste des messages pour pouvoir les récupérer dans le onResume

        //Load last position known
        if(myPosition != null) {
            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putFloat(SHARED_PREF_LAT, (float) myPosition.getLatitude());
            editor.putFloat(SHARED_PREF_LON, (float) myPosition.getLongitude());
            //TODO: à vérifier (commit --> apply)
            editor.apply();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        super.onLocationChanged(location);

        if(firstTime) {
            mapController.setCenter(myPosition);
            firstTime = false;
        }

        locationOverlay.setLocation(myPosition);
    }

    public void onClickCenterPosition(View view) {
        if(myPosition != null) {
            Toast.makeText(this, "Your are at position : " + myPosition, Toast.LENGTH_SHORT).show();
            mapController.animateTo(myPosition);
        } else {
            Toast.makeText(this, "Unknown position", Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickAugmentedView(View view) {
        //TODO: replier le menu de floating buttons
        Intent intent = new Intent(this, AugmentedViewActivity.class);
        HashMap<String, Message> test = new HashMap<>();

        for(String key : messages.keySet()) {
            Log.i(TAG, key + " is " + messages.get(key));
        }
        intent.putExtra(LocatedActivity.INTENT_MESSAGES, test);
        startActivity(intent);
    }

    @Override
    public void onMessageAdded(Message msg) {
        Log.i(TAG, "onMessageAdded: " + msg);
        mItemOverlay.addItem(new OverlayItem(msg.getUrl(), "SampleDescription", new GeoPoint(msg.getLatitude(), msg.getLongitude())));
        mapView.invalidate();
    }

    @Override
    public void onMessageRemoved(Message msg) {
        Log.i(TAG, "onMessageRemoved: " + msg);
    }
}
