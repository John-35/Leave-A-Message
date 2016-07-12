package com.example.externe.leave_a_message.activities;

import android.content.Intent;
import android.location.Location;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.externe.leave_a_message.R;
import com.example.externe.leave_a_message.utils.App_Const;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpPost;
import com.koushikdutta.async.http.AsyncHttpResponse;
import com.koushikdutta.async.http.body.MultipartFormDataBody;
import com.koushikdutta.async.http.callback.HttpConnectCallback;

public class DrawActivity extends AppCompatActivity {


    private static final String TAG = "DrawActivity";

    private Location mLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);

        Intent intent = getIntent();
        if(intent != null) {
            mLocation = intent.getParcelableExtra(DisplayMapActivity.INTENT_LOCATION);
        } else {
            Toast.makeText(this, "Erreur lors de la récupération de la position", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    public void onClickBtnSend(View view) {
        if(mLocation != null) {
            AsyncHttpPost post = new AsyncHttpPost(App_Const.URL_UPLOAD);
            MultipartFormDataBody body = new MultipartFormDataBody();

            String message = "<msg lat='" + mLocation.getLatitude() + "' lng='" + mLocation.getLongitude() + "'>";
            /*for (int i = 0; i < 20; i++) {
                message += "<pt x='10' y='100' z='1000'/>";
            }*/
            message += ((EditText)findViewById(R.id.edittxt_msg)).getText().toString();
            message += "</msg>";
            body.addStringPart("msg", message);
            body.addStringPart("lat", String.valueOf(mLocation.getLatitude()));
            body.addStringPart("lng", String.valueOf(mLocation.getLongitude()));
            post.setBody(body);

            AsyncHttpClient.getDefaultInstance().execute(post, new HttpConnectCallback() {
                @Override
                public void onConnectCompleted(Exception ex, AsyncHttpResponse res) {
                    Log.i(TAG, "Uploaded: " + res);
                    Toast.makeText(DrawActivity.this, "Resultat: " + res, Toast.LENGTH_SHORT).show();
                }
            });
        }

        Toast.makeText(this, "Message envoyé", Toast.LENGTH_SHORT).show();

        finish();
    }
}
