package com.demomap.nzyn.nzyndemomap.model;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dangd on 7/6/17.
 */

public class Route {
    public static final String JSON_KEY_OVERVIEW_POLYLINE = "overview_polyline";
    public static final String JSON_KEY_LEGS = "legs";
    public static final String JSON_KEY_POINTS = "points";
    private Bounds bounds;
    private String copyrights;
    private List<Leg> legs = new ArrayList<>();
    private String summary;
    private List<Object> warnings = null;
    private List<Object> waypointOrder = null;
    private List<LatLng> points;


    public Route(JSONObject data){
        if(data != null){
            parseJson(data);
        }
    }

    private void parseJson(JSONObject data){
        JSONArray jsonArrayLegs = data.optJSONArray(JSON_KEY_LEGS);
        for(int i=0; i<jsonArrayLegs.length(); i++){
            Leg leg = new Leg(jsonArrayLegs.optJSONObject(i));
            legs.add(leg);
        }
        JSONObject jsonOverviewPolyline = data.optJSONObject(JSON_KEY_OVERVIEW_POLYLINE);
        points = polyLineDecode(jsonOverviewPolyline.optString(JSON_KEY_POINTS));
    }

    public Bounds getBounds() {
        return bounds;
    }

    public void setBounds(Bounds bounds) {
        this.bounds = bounds;
    }

    public String getCopyrights() {
        return copyrights;
    }

    public void setCopyrights(String copyrights) {
        this.copyrights = copyrights;
    }

    public List<Leg> getLegs() {
        return legs;
    }

    public void setLegs(List<Leg> legs) {
        this.legs = legs;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public List<Object> getWarnings() {
        return warnings;
    }

    public void setWarnings(List<Object> warnings) {
        this.warnings = warnings;
    }

    public List<Object> getWaypointOrder() {
        return waypointOrder;
    }

    public void setWaypointOrder(List<Object> waypointOrder) {
        this.waypointOrder = waypointOrder;
    }

    public List<LatLng> getPoints() {
        return points;
    }

    public void setPoints(List<LatLng> points) {
        this.points = points;
    }

    private List<LatLng> polyLineDecode(final String poly) {
        int len = poly.length();
        int index = 0;
        List<LatLng> decoded = new ArrayList<LatLng>();
        int lat = 0;
        int lng = 0;

        while (index < len) {
            int b;
            int shift = 0;
            int result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = poly.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            decoded.add(new LatLng(
                    lat / 100000d, lng / 100000d
            ));
        }

        return decoded;
    }
}
