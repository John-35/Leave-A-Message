package fr.jonathanperrinet.leave_a_message.utils;

import android.content.Context;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import fr.jonathanperrinet.leave_a_message.model.BezierCurve;
import fr.jonathanperrinet.leave_a_message.model.Message;
import fr.jonathanperrinet.leave_a_message.model.ParcelableVector3;

/**
 * Created by Jonathan Perrinet on 08/08/2016.
 */
public class MessageManager {

    private static final double MAX_DISTANCE = 0.5; // km
    private static final String TAG = "MessageManager";

    public interface OnMessageListener {
        public void onMessageReceived(String url, int type, double latitude, double longitude);
    }

    public static void downloadMessages(final Context context, GeoPoint location) throws ClassCastException {
        //TODO: check interface
        if(!(context instanceof OnMessageListener)) {
            throw new ClassCastException(context.toString() + " must implements MessageManager.OnMessageListener");
        }

        Ion.with(context)
                .load(App_Const.URL_LIST)
                .setMultipartParameter("lat", String.valueOf(location.getLatitude()))
                .setMultipartParameter("lng", String.valueOf(location.getLongitude()))
                .setMultipartParameter("dist", String.valueOf(MAX_DISTANCE))
                .asJsonArray()
                .setCallback(new FutureCallback<JsonArray>() {
                    @Override
                    public void onCompleted(Exception e, com.google.gson.JsonArray jsonArray) {
                        if(jsonArray != null) {
                            Iterator<JsonElement> iterator = jsonArray.iterator();
                            JsonObject jsonObject;
                            while(iterator.hasNext()) {
                                jsonObject = iterator.next().getAsJsonObject();
                                double latitude = jsonObject.get("lat").getAsDouble();
                                double longitude = jsonObject.get("lng").getAsDouble();
                                String url = jsonObject.get("url").getAsString();
                                int type = Message.TYPE_DRAW;//jsonObject.get("type").getAsInt();
                                ((OnMessageListener)(context)).onMessageReceived(url, type, latitude, longitude);
                            }
                        }
                    }
                });
    }

    public static void openMessageFromServer(Context context, final Message msg) {
        Ion.with(context)
                .load(App_Const.URL_GET_MESSAGE)
                .setMultipartParameter("url", msg.getUrl())
                .asString()
                .setCallback(new FutureCallback<String>() {
                    @Override
                    public void onCompleted(Exception e, String json) {
                        Log.i(TAG, json);
                        //TODO: récupérer les données du message
                        if(json != null) {
                            try {
                                JSONObject jsonObj = new JSONObject(json);

                                if(jsonObj.has(Message.ATTR_TYPE)) {
                                    int type = jsonObj.getInt(Message.ATTR_TYPE);
                                    if(type == Message.TYPE_DRAW) {
                                        List<BezierCurve> curves = new ArrayList<>();
                                        float rotX = getRotation(jsonObj, Message.ATTR_ROTX);
                                        float rotY = getRotation(jsonObj, Message.ATTR_ROTY);
                                        float rotZ = getRotation(jsonObj, Message.ATTR_ROTZ);
                                        List<BezierCurve> listCurves = new ArrayList<>();
                                        if(jsonObj.has(Message.ATTR_POINTS)) {
                                            JSONArray jArrayCurve = jsonObj.getJSONArray(Message.ATTR_POINTS);
                                            for(int i = 0 ; i < jArrayCurve.length() ; i++) { //curves
                                                JSONArray jArrayPoints = jArrayCurve.getJSONArray(i);
                                                if(jArrayPoints.length() == 4) {
                                                    ParcelableVector3[] curvePoints = new ParcelableVector3[4];
                                                    for(int j = 0 ; j < 4 ; j++) { //points
                                                        JSONArray point = jArrayPoints.getJSONArray(j);
                                                        curvePoints[j] = new ParcelableVector3((float) point.getDouble(0),
                                                                                                (float) point.getDouble(1),
                                                                                                (float) point.getDouble(2));
                                                    }
                                                    try {
                                                        curves.add(new BezierCurve(curvePoints));
                                                    } catch (Exception e1) {
                                                        Log.e(TAG, e1.getMessage());
                                                    }
                                                }

                                            }
                                        }
                                        msg.setContent(curves);
                                    } else {
                                        msg.setContent(jsonObj.toString());
                                    }
                                }
                                Log.i(TAG, "type: " + jsonObj);
                            } catch (JSONException je) {
                                Log.e(TAG, "Erreur : " + je.getMessage());
                            }
                        } else {
                            //((MessageString) msg).setMessage(getResources().getString(R.string.erreur_loading_message));
                        }
                        msg.setLoaded(true);
                    }
                });
    }

    private static float getRotation(JSONObject jObj, String attr) {
        try {
            return (float)jObj.getDouble(attr);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return 0.0f;
    }
}
