package com.demomap.nzyn.nzyndemomap.model;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONObject;

/**
 * Created by dangd on 7/8/17.
 */

public class Location {
    private static final String JSON_KEY_LAT = "lat";
    private static final String JSON_KEY_LNG = "lng";
    private Double lat;
    private Double lng;

    public Location(JSONObject data){
        parseJson(data);
    }

    private void parseJson(JSONObject data){
        if(data == null){
            return;
        }
        lat = data.optDouble(JSON_KEY_LAT);
        lng = data.optDouble(JSON_KEY_LNG);
    }

    public Double getLat() {
        return lat;
    }

    public void setLat(Double lat) {
        this.lat = lat;
    }

    public Double getLng() {
        return lng;
    }

    public void setLng(Double lng) {
        this.lng = lng;
    }

    public LatLng getLatLng(){
        return new LatLng(getLat(), getLng());
    }

}
