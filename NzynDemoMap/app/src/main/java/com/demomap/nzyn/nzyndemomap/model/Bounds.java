package com.demomap.nzyn.nzyndemomap.model;

import org.json.JSONObject;

/**
 * Created by dangd on 7/8/17.
 */

public class Bounds {
    private static final String JSON_KEY_NORTH_EAST_BOUND = "northeast";
    private static final String JSON_KEY_SOUTH_WEST_BOUND = "southwest";
    private Location northeast;
    private Location southwest;

    public Bounds(JSONObject data){
        parseJson(data);
    }

    private void parseJson(JSONObject data){
        if(data == null){
            return;
        }
        northeast = new Location(data.optJSONObject(JSON_KEY_NORTH_EAST_BOUND));
        southwest = new Location(data.optJSONObject(JSON_KEY_SOUTH_WEST_BOUND));
    }

    public Location getNortheast() {
        return northeast;
    }

    public void setNortheast(Location northeast) {
        this.northeast = northeast;
    }

    public Location getSouthwest() {
        return southwest;
    }

    public void setSouthwest(Location southwest) {
        this.southwest = southwest;
    }
}
