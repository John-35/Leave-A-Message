package fr.jonathanperrinet.leave_a_message.activities;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import org.osmdroid.util.GeoPoint;

import java.util.HashMap;

import fr.jonathanperrinet.leave_a_message.model.Message;
import fr.jonathanperrinet.leave_a_message.model.MessageDrawn;
import fr.jonathanperrinet.leave_a_message.model.MessageString;
import fr.jonathanperrinet.leave_a_message.utils.MessageManager;

/**
 * Created by Jonathan Perrinet on 09/08/2016.
 */
public abstract class LocatedActivity extends AppCompatActivity implements LocationListener, MessageManager.OnMessageListener {
    private static final String TAG = "LocatedActivity";

    public static final String INTENT_MESSAGES = "messages";

    private LocationManager locationManager;

    private final long MIN_TIME = 500; //milliseconds
    private final float GPS_MIN_DIST = 1; //meters

    protected GeoPoint myPosition = null;

    protected HashMap<String, Message> messages = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(TAG, "onCreate");
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        boolean enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!enabled) {
            //TODO: gestion position indisponible
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME, GPS_MIN_DIST, this);
            Log.i(TAG, "Registering location manager");
        } catch (SecurityException se) {
            se.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        try {
            locationManager.removeUpdates(this);
            Log.i(TAG, "Unregistering location manager");
        } catch (SecurityException se) {
            se.printStackTrace();
        }
    }


    @Override
    public void onLocationChanged(Location location) {
        myPosition = new GeoPoint(location.getLatitude(), location.getLongitude());
        MessageManager.downloadMessages(this, myPosition);
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onMessageReceived(String url, int type, double latitude, double longitude) {
        //Log.i(TAG, "onMessageReceived " + url);
        if(!messages.containsKey(url)) {
            Message msg = null;
            Log.i(TAG, "onMessageReceived: " + url + " (" + type + ")");
            if(type == Message.TYPE_DRAW) {
                msg = new MessageDrawn(url, latitude, longitude);
            } else if(type == Message.TYPE_TEXT) {
                msg = new MessageString(url, latitude, longitude);
            }
            if(msg != null) {
                messages.put(url, msg);
                //Log.i(TAG, "put " + messages);
                onMessageAdded(msg);
                MessageManager.openMessageFromServer(this, msg);
            }
        }
    }

    public abstract void onMessageAdded(Message msg);

    public abstract void onMessageRemoved(Message msg);
}
