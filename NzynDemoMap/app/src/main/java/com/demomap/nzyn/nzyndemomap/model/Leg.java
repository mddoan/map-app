package com.demomap.nzyn.nzyndemomap.model;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by dangd on 7/8/17.
 */

public class Leg {
    private static final String JSON_KEY_END_ADDRESS = "end_address";
    private static final String JSON_KEY_START_ADDRESS = "start_address";
    private static final String JSON_KEY_START_LOCATION = "start_location";
    private static final String JSON_KEY_END_LOCATION = "end_location";
    private static final String JSON_KEY_DURATION = "duration";
    private static final String JSON_KEY_DISTANCE = "distance";
    private Distance distance;
    private Duration duration;
    private String endAddress;
    private Location endLocation;
    private String startAddress;
    private Location startLocation;
    private List<Step> steps = null;
    private List<Object> trafficSpeedEntry = null;
    private List<Object> viaWaypoint = null;

    public Leg(JSONObject data){
        parseJson(data);
    }

    private void parseJson(JSONObject data){
        if(data == null) {
            return;
        }
        distance = new Distance(data.optJSONObject(JSON_KEY_DISTANCE));
        duration = new Duration(data.optJSONObject(JSON_KEY_DURATION));
        startAddress = data.optString(JSON_KEY_START_ADDRESS);
        endAddress = data.optString(JSON_KEY_END_ADDRESS);
        JSONObject jsonStartLocation = data.optJSONObject(JSON_KEY_START_LOCATION);
        startLocation = new Location(jsonStartLocation);
        JSONObject jsonEndLocation = data.optJSONObject(JSON_KEY_END_LOCATION);
        endLocation = new Location(jsonEndLocation);
    }

    public Distance getDistance() {
        return distance;
    }

    public void setDistance(Distance distance) {
        this.distance = distance;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public String getEndAddress() {
        return endAddress;
    }

    public void setEndAddress(String endAddress) {
        this.endAddress = endAddress;
    }

    public Location getEndLocation() {
        return endLocation;
    }

    public void setEndLocation(Location endLocation) {
        this.endLocation = endLocation;
    }

    public String getStartAddress() {
        return startAddress;
    }

    public void setStartAddress(String startAddress) {
        this.startAddress = startAddress;
    }

    public Location getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(Location startLocation) {
        this.startLocation = startLocation;
    }

    public List<Step> getSteps() {
        return steps;
    }

    public void setSteps(List<Step> steps) {
        this.steps = steps;
    }

    public List<Object> getTrafficSpeedEntry() {
        return trafficSpeedEntry;
    }

    public void setTrafficSpeedEntry(List<Object> trafficSpeedEntry) {
        this.trafficSpeedEntry = trafficSpeedEntry;
    }

    public List<Object> getViaWaypoint() {
        return viaWaypoint;
    }

    public void setViaWaypoint(List<Object> viaWaypoint) {
        this.viaWaypoint = viaWaypoint;
    }

}
