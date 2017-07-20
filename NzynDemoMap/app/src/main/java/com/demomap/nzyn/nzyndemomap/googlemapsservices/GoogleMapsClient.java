package com.demomap.nzyn.nzyndemomap.googlemapsservices;

import android.os.AsyncTask;
import android.support.v4.os.AsyncTaskCompat;
import android.util.Log;

import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by dangd on 7/7/17.
 */

public class GoogleMapsClient extends AsyncTask<String, Void, String>{
    public static final String TAG = GoogleMapsClient.class.getName();
    private GoogleMapsClientListener listener;

    @Override
    protected String doInBackground(String... params) {
        String link = params[0];
        try {
            URL url = new URL(link);
            InputStream is = url.openConnection().getInputStream();
            StringBuffer buffer = new StringBuffer();
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }
            final String response = buffer.toString();
            Log.v(TAG, "response: " + response);
            return response;

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(String res) {
        listener.onPostExecute(res);
    }

    public interface GoogleMapsClientListener{
        void onPostExecute(String res);
    }

    public void setListener(GoogleMapsClientListener listener){
        this.listener = listener;
    }
}
