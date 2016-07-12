package fr.jonathanperrinet.leave_a_message.activities;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import fr.jonathanperrinet.leave_a_message.models.MessageString;
import fr.jonathanperrinet.leave_a_message.utils.App_Const;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(fr.jonathanperrinet.leave_a_message.R.layout.activity_main);

        TextView tv = (TextView)findViewById(fr.jonathanperrinet.leave_a_message.R.id.txtview_title);
        tv.setTypeface(Typeface.createFromAsset(getAssets(), "fonts/Polyline.ttf"));

        if(Build.VERSION.SDK_INT >= 23) {
            checkPermissions();
        }
    }

    final private int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 124;

    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermissions() {
        List<String> permissions = new ArrayList<>();
        String message = "osmdroid permissions:";
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            message += "\nLocation to show user location.";
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            message += "\nStorage access to store map tiles.";
        }
        if (!permissions.isEmpty()) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            String[] params = permissions.toArray(new String[permissions.size()]);
            requestPermissions(params, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
        } // else: We already have permissions, so handle as normal
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS:	{
                Map<String, Integer> perms = new HashMap<>();
                // Initial
                perms.put(Manifest.permission.ACCESS_FINE_LOCATION, PackageManager.PERMISSION_GRANTED);
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                // Check for ACCESS_FINE_LOCATION and WRITE_EXTERNAL_STORAGE
                Boolean location = perms.get(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
                Boolean storage = perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
                if (location && storage) {
                    // All Permissions Granted
                    Toast.makeText(MainActivity.this, "All permissions granted", Toast.LENGTH_SHORT).show();
                } else if (location) {
                    Toast.makeText(this, "Storage permission is required to store map tiles to reduce data usage and for offline usage.", Toast.LENGTH_LONG).show();
                } else if (storage) {
                    Toast.makeText(this,"Location permission is required to show the user's location on map.", Toast.LENGTH_LONG).show();
                } else { // !location && !storage case
                    // Permission Denied
                    Toast.makeText(MainActivity.this, "Storage permission is required to store map tiles to reduce data usage and for offline usage." +
                            "\nLocation permission is required to show the user's location on map.", Toast.LENGTH_SHORT).show();
                }
            }
            break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public void onClickBtnMap(View view) {
        Intent intent = new Intent(this, DisplayMapActivity.class);
        startActivity(intent);
    }

    public void onClickBtnTest(View view) {
        Ion.with(this)
                .load(App_Const.URL_GETMESSAGE)
                .setMultipartParameter("lat", String.valueOf(48.107175536209155))
                .setMultipartParameter("lng", String.valueOf(-1.693147445715343))
                .setMultipartParameter("dist", String.valueOf(1))
                .asJsonArray()
                .setCallback(new FutureCallback<com.google.gson.JsonArray>() {
                    @Override
                    public void onCompleted(Exception e, com.google.gson.JsonArray jsonArray) {
                        ArrayList<MessageString> listMsg = new ArrayList<>();
                        Iterator<JsonElement> iterator = jsonArray.iterator();
                        JsonObject jsonObject;
                        while(iterator.hasNext()) {
                            jsonObject = iterator.next().getAsJsonObject();
                            double latitude = jsonObject.get("lat").getAsDouble();
                            double longitude = jsonObject.get("lng").getAsDouble();
                            String url = jsonObject.get("url").getAsString();
                            Toast.makeText(MainActivity.this, "Url: " + url, Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }
}
