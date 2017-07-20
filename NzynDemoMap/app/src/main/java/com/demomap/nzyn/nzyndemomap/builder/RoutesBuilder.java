package com.demomap.nzyn.nzyndemomap.builder;

import android.content.Context;
import android.util.Log;

import com.demomap.nzyn.nzyndemomap.MapsActivity;
import com.demomap.nzyn.nzyndemomap.googlemapsservices.DirectionRequest;
import com.demomap.nzyn.nzyndemomap.googlemapsservices.GoogleMapsClient;
import com.demomap.nzyn.nzyndemomap.model.Route;

import org.json.JSONException;

import java.util.List;

/**
 * Created by dangd on 7/6/17.
 */

public class RoutesBuilder implements GoogleMapsClient.GoogleMapsClientListener{
    public static final String TAG = RoutesBuilder.class.getName();
    Context mContext;
    private RoutesBuilderListener listener;

    public RoutesBuilder(Context context){
        mContext = context;
        listener = (MapsActivity)context;
    }

    public void build(String start, String end, String avoid, String mode) throws Exception{
        String requestUrl = new DirectionRequest(mContext, start, end, avoid, mode).getUrl();
        Log.v(TAG, "Url = " + requestUrl);
        GoogleMapsClient googleMapsClient = new GoogleMapsClient();
        googleMapsClient.setListener(this);
        googleMapsClient.execute(requestUrl);

    }

    @Override
    public void onPostExecute(String response){
        try {
            List<Route> routes = DirectionRequest.parseJson(response);
            listener.onRouteBuilderSuccess(routes);
        }catch (JSONException e){
            Log.e(TAG, "JSONException e: " + e);
            listener.onRouteBuilderFailure();
        }
    }

    public interface RoutesBuilderListener{
        void onRouteBuilderSuccess(List<Route> routes);
        void onRouteBuilderFailure();
    }

}
