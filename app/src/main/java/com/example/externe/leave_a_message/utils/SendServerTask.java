package com.example.externe.leave_a_message.utils;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.example.externe.leave_a_message.models.Point3D;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class SendServerTask extends AsyncTask<ArrayList<Point3D>, Void, Boolean> {

    private static final String TAG = "SendServerTask" ;

    AsyncListener listenerActivity;

    public SendServerTask(AsyncListener listener) {
        listenerActivity = listener;
    }

    @Override
    protected Boolean doInBackground(ArrayList<Point3D>... params) {
        Boolean result = null;

        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), "newfile");

        try {
            URL url = new URL("");

            Log.d(TAG, "uri : " + url);

            HttpURLConnection httpURLConnection = (HttpURLConnection)url.openConnection();
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);


            OutputStream out = new BufferedOutputStream(httpURLConnection.getOutputStream());

            /*InputStream is = httpURLConnection.getInputStream();
            InputStreamReader reader = new InputStreamReader(is);

            int inChar;
            final StringBuilder readStr = new StringBuilder();
                while((inChar = reader.read()) != -1) {
                    readStr.append((char)inChar);
                }
                result = readStr.toString();
               */
            httpURLConnection.disconnect();
        } catch (IOException ioe) {
                ioe.printStackTrace();
        }

        return result;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);

        listenerActivity.onDataReceived(result);
        //parseJson(s);
    }

    public interface AsyncListener {
        void onDataReceived(boolean result);
    }

}
