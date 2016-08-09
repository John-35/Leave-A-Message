package fr.jonathanperrinet.leave_a_message.activities;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by Jonathan Perrinet on 09/08/2016.
 */
public abstract class LocatedActivity extends AppCompatActivity implements LocationListener {
    private static final String TAG = "LocatedActivity";

    private LocationManager locationManager;

    private final long MIN_TIME = 500; //milliseconds
    private final float GPS_MIN_DIST = 1; //meters

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
    public abstract void onLocationChanged(Location location);

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
