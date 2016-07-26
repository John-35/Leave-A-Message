package fr.jonathanperrinet.leave_a_message.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpPost;
import com.koushikdutta.async.http.AsyncHttpResponse;
import com.koushikdutta.async.http.body.MultipartFormDataBody;
import com.koushikdutta.async.http.callback.HttpConnectCallback;

import fr.jonathanperrinet.leave_a_message.leave_a_message.R;
import fr.jonathanperrinet.leave_a_message.utils.App_Const;

public class DrawActivity extends AppCompatActivity {

    private static final String TAG = "DrawActivity";

    private double latitude;
    private double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);

        Intent intent = getIntent();
        if(intent != null) {
            latitude = intent.getDoubleExtra(DisplayMapActivity.INTENT_LOCATION_LAT, 0);
            longitude = intent.getDoubleExtra(DisplayMapActivity.INTENT_LOCATION_LONG, 0);
        } else {
            Toast.makeText(this, "Erreur lors de la récupération de la position", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    public void onClickBtnSend(View view) {
        AsyncHttpPost post = new AsyncHttpPost(App_Const.URL_UPLOAD);
        MultipartFormDataBody body = new MultipartFormDataBody();

        //String message = "{ lat='" + mLocation.getLatitude() + "' lng='" + mLocation.getLongitude() + "'>";
        String message = "{" + ((EditText)findViewById(R.id.edittxt_msg)).getText().toString() + "}";
        body.addStringPart("msg", message);
        body.addStringPart("lat", String.valueOf(latitude));
        body.addStringPart("lng", String.valueOf(longitude));
        post.setBody(body);

        AsyncHttpClient.getDefaultInstance().execute(post, new HttpConnectCallback() {
            @Override
            public void onConnectCompleted(Exception ex, AsyncHttpResponse res) {
                Log.i(TAG, "Uploaded: " + res);
                Toast.makeText(DrawActivity.this, "Resultat: " + res, Toast.LENGTH_SHORT).show();
            }
        });

        Toast.makeText(this, "Message envoyé", Toast.LENGTH_SHORT).show();

        finish();
    }
}
