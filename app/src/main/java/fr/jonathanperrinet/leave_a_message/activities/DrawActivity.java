package fr.jonathanperrinet.leave_a_message.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.github.gcacace.signaturepad.views.SignaturePad;

import java.util.ArrayList;

import fr.jonathanperrinet.leave_a_message.leave_a_message.R;
import fr.jonathanperrinet.leave_a_message.model.BezierCurve;

/**
 * Created by Jonathan Perrinet.
 */
public class DrawActivity extends AppCompatActivity {

    public static final int REQUEST_CODE = 1;

    private static final String TAG = "DrawActivity";

    private double latitude;
    private double longitude;

    private SignaturePad pad;

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

        pad = (SignaturePad)findViewById(R.id.pad);
        pad.setOnSignedListener(new SignaturePad.OnSignedListener() {
            @Override
            public void onStartSigning() {

            }

            @Override
            public void onSigned() {

            }

            @Override
            public void onClear() {

            }
        });
    }

    public void onClickBtnSend(View view) {
        /*AsyncHttpPost post = new AsyncHttpPost(App_Const.URL_UPLOAD);
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
        */
        //finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE) {
            if(resultCode == RESULT_OK) {
                Toast.makeText(this, "Result", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onClickBtnPositionMsg(View view) {
        Intent intent = new Intent(DrawActivity.this, AugmentedViewActivity.class);
        intent.putExtra("svg", pad.getSignatureSvg());
        ArrayList<BezierCurve> curves = pad.getBeziersCurves();
        intent.putExtra("curves", curves);
        //startActivityForResult(intent, REQUEST_CODE);
        startActivity(intent);
    }
}
