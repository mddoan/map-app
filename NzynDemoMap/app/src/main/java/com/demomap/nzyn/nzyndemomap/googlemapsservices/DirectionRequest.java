package com.demomap.nzyn.nzyndemomap.googlemapsservices;

import android.content.Context;
import android.text.TextUtils;

import com.demomap.nzyn.nzyndemomap.R;
import com.demomap.nzyn.nzyndemomap.model.Route;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dangd on 7/7/17.
 */

public class DirectionRequest {
    private String mUrl;
    public DirectionRequest(Context context, String origin, String destination, String avoid, String mode){
        try {
            String encodeOrigin = URLEncoder.encode(origin, "utf-8");
            String encodeDestination = URLEncoder.encode(destination, "utf-8");
            mUrl = new StringBuilder().append(context.getString(R.string.base_url)).
                    append(encodeOrigin).
                    append("&destination=").
                    append(encodeDestination).
                    append(TextUtils.isEmpty(avoid)? "":"&avoid=").
                    append(TextUtils.isEmpty(avoid)? "":avoid).
                    append(TextUtils.isEmpty(mode)? "":"&mode=").
                    append(TextUtils.isEmpty(mode)? "":mode).
                    append("&alternatives=true").
                    append("&key=").
                    append(context.getString(R.string.google_maps_key)).toString();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }


    }
    public String getUrl(){
        return mUrl;
    }

    public static List<Route> parseJson(String response) throws JSONException{
        if(response == null){
            return null;
        }
        JSONObject jsonResponse = new JSONObject(response);
        JSONArray jsonRoutes = jsonResponse.getJSONArray("routes");
        List<Route> routes = new ArrayList<>();
        for (int i = 0; i < jsonRoutes.length(); i++) {
            JSONObject jsonRoute = jsonRoutes.optJSONObject(i);
            Route route = new Route(jsonRoute);
            routes.add(route);
        }
        return routes;
    }

}
