package fr.jonathanperrinet.leave_a_message.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;
import android.widget.ViewSwitcher;

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
import fr.jonathanperrinet.leave_a_message.model.MessageDrawn;
import fr.jonathanperrinet.leave_a_message.model.MessageString;
import fr.jonathanperrinet.leave_a_message.utils.App_Const;

/**
 * Created by Jonathan Perrinet.
 */
public class DrawActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    public static final int REQUEST_CODE = 1;

    private static final String TAG = "DrawActivity";

    private double latitude;
    private double longitude;

    private AppCompatEditText editText;
    private ViewSwitcher switcher;
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

        switcher = (ViewSwitcher)findViewById(R.id.switcher);

        editText = (AppCompatEditText)findViewById(R.id.editTextMsg);

        SwitchCompat switchCompat = (SwitchCompat)findViewById(R.id.switchBtt);
        switchCompat.setOnCheckedChangeListener(this);
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
        //TODO: ajouter l'ajout d'un contact/mail lors de l'upload d'un message
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
                //TODO: Demander avant d'envoyer le sms
                sendSms(latitude, longitude);
            }
        });

        Toast.makeText(this, "Message envoyé", Toast.LENGTH_SHORT).show();
    }

    private void sendSms(double latitude, double longitude) {
        //TODO: send sms to someone
        String content = "Un message vous attend ici : " + Uri.parse("http://maps.google.com/?q=<" + latitude + ">,<" + longitude + ">");
        Intent sendIntent = new Intent(Intent.ACTION_VIEW);
        sendIntent.putExtra("sms_body", content);
        sendIntent.setType("vnd.android-dir/mms-sms");
        startActivity(sendIntent);
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
                .append(rotZ);

        if(mode == Message.TYPE_DRAW) {
            builder.append(", '" + Message.ATTR_POINTS + "': [");

            ArrayList<BezierCurve> curves = pad.getBeziersCurves();
            for (BezierCurve curve : curves) {
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
        } else if(mode == Message.TYPE_TEXT) {
            builder.append(", '" + Message.ATTR_TEXT + "':\"");
            builder.append(editText.getText().toString());
            builder.append("\"}");
        }

        return builder.toString();
    }

    public void onClickPlaceBtn(View view) {
        //TODO: créer une instance de message et la transmettre
        Intent intent = new Intent(DrawActivity.this, AugmentedViewActivity.class);
        //ArrayList<BezierCurve> curves = pad.getBeziersCurves();
        //intent.putExtra("curves", curves);
        Message msg = null;
        if(mode == Message.TYPE_DRAW) {
            msg = new MessageDrawn("new");
            msg.setContent(pad.getBeziersCurves());
            msg.setLoaded(true);
        } else if(mode == Message.TYPE_TEXT) {
            msg = new MessageString("new");
            msg.setLoaded(true);
            if(editText != null)
                msg.setContent(editText.getText().toString());
        }
        ArrayList<Message> listMsg = new ArrayList<>();
        listMsg.add(msg);
        intent.putParcelableArrayListExtra(LocatedActivity.INTENT_MESSAGES, listMsg);
        fam.collapse();
        startActivityForResult(intent, REQUEST_CODE);
    }

    public void onClickEraseEdit(View view) {
        pad.clear();
        fam.collapse();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switcher.showNext();
        mode = (mode == Message.TYPE_DRAW) ? Message.TYPE_TEXT : Message.TYPE_DRAW;
    }
}
