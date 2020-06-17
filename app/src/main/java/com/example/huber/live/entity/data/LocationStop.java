package com.example.huber.live.entity.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LocationStop {

    @SerializedName("properties")
    @Expose
    private Properties properties;

    public Properties getProperties() {
        return properties;
    }

}
