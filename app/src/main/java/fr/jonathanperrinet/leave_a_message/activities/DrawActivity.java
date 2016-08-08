package fr.jonathanperrinet.leave_a_message.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.github.gcacace.signaturepad.views.SignaturePad;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.AsyncHttpPost;
import com.koushikdutta.async.http.AsyncHttpResponse;
import com.koushikdutta.async.http.body.MultipartFormDataBody;
import com.koushikdutta.async.http.callback.HttpConnectCallback;

import java.util.ArrayList;

import fr.jonathanperrinet.leave_a_message.leave_a_message.R;
import fr.jonathanperrinet.leave_a_message.model.BezierCurve;
import fr.jonathanperrinet.leave_a_message.model.Message;
import fr.jonathanperrinet.leave_a_message.utils.App_Const;

/**
 * Created by Jonathan Perrinet.
 */
public class DrawActivity extends AppCompatActivity {

    public static final int REQUEST_CODE = 1;

    private static final String TAG = "DrawActivity";

    //TODO: mettre à jour la position en permanence. Mise en place d'un service ?
    private double latitude;
    private double longitude;

    private SignaturePad pad;
    private FloatingActionsMenu fam;

    private int mode = Message.TYPE_DRAW;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_draw);

        fam = (FloatingActionsMenu)findViewById(R.id.floating_menu);

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE && resultCode != RESULT_CANCELED) {
            float rotX = data.getFloatExtra(AugmentedViewActivity.INTENT_ROT_X, 0);
            float rotY = data.getFloatExtra(AugmentedViewActivity.INTENT_ROT_Y, 0);
            float rotZ = data.getFloatExtra(AugmentedViewActivity.INTENT_ROT_Z, 0);

            sendMessage(rotX, rotY, rotZ);
        }
    }

    private void sendMessage(float rotX, float rotY, float rotZ) {
        AsyncHttpPost post = new AsyncHttpPost(App_Const.URL_UPLOAD);
        MultipartFormDataBody body = new MultipartFormDataBody();

        String message = formatMessage(rotX, rotY, rotZ);
        body.addStringPart("msg", message);
        body.addStringPart("lat", String.valueOf(latitude));
        body.addStringPart("lng", String.valueOf(longitude));
        body.addStringPart("type", String.valueOf(mode));
        post.setBody(body);

        AsyncHttpClient.getDefaultInstance().execute(post, new HttpConnectCallback() {
            @Override
            public void onConnectCompleted(Exception ex, AsyncHttpResponse res) {
                Log.i(TAG, "Uploaded: " + res);
                Toast.makeText(DrawActivity.this, "Resultat: " + res.message(), Toast.LENGTH_SHORT).show();
            }
        });

        Toast.makeText(this, "Message envoyé", Toast.LENGTH_SHORT).show();
    }

    private String formatMessage(float rotX, float rotY, float rotZ) {
        StringBuilder builder = new StringBuilder("{")
                .append("'" + Message.ATTR_TYPE + "':")
                .append(mode)
                .append(",'" + Message.ATTR_ROTX + "':")
                .append(rotX)
                .append(",'" + Message.ATTR_ROTY + "':")
                .append(rotY)
                .append(",'" + Message.ATTR_ROTZ + "':")
                .append(rotZ)
                .append(", '" + Message.ATTR_POINTS + "': [");

        ArrayList<BezierCurve> curves = pad.getBeziersCurves();
        for(BezierCurve curve : curves) {
            builder.append("[[");
            builder.append(curve.startPoint.x);
            builder.append(",");
            builder.append(curve.startPoint.y);
            builder.append(",");
            builder.append(curve.startPoint.z);
            builder.append("],[");
            builder.append(curve.control1.x);
            builder.append(",");
            builder.append(curve.control1.y);
            builder.append(",");
            builder.append(curve.control1.z);
            builder.append("],[");
            builder.append(curve.control2.x);
            builder.append(",");
            builder.append(curve.control2.y);
            builder.append(",");
            builder.append(curve.control2.z);
            builder.append("],[");
            builder.append(curve.endPoint.x);
            builder.append(",");
            builder.append(curve.endPoint.y);
            builder.append(",");
            builder.append(curve.endPoint.z);
            builder.append("]],");
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append("]}");

        return builder.toString();
    }

    public void onClickPlaceBtn(View view) {
        Log.i(TAG, "onClickPlaceBtn");
        Intent intent = new Intent(DrawActivity.this, AugmentedViewActivity.class);
        ArrayList<BezierCurve> curves = pad.getBeziersCurves();
        intent.putExtra("curves", curves);
        fam.collapse();
        startActivityForResult(intent, REQUEST_CODE);
    }

    public void onClickEraseEdit(View view) {
        pad.clear();
        fam.collapse();
    }
}
