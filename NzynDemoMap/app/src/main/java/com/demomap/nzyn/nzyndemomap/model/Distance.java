package com.demomap.nzyn.nzyndemomap.model;

import org.json.JSONObject;

/**
 * Created by dangd on 7/6/17.
 */

public class Distance {
    private static final String JSON_KEY_TEXT = "text";
    private static final String JSON_KEY_VALUE = "value";
    private String text;
    private Integer value;

    public Distance(JSONObject data){
        parseJson(data);
    }

    private void parseJson(JSONObject data){
        if(data == null){
            return;
        }
        text = data.optString(JSON_KEY_TEXT);
        value = data.optInt(JSON_KEY_VALUE);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Distance withText(String text) {
        this.text = text;
        return this;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public Distance withValue(Integer value) {
        this.value = value;
        return this;
    }

}

