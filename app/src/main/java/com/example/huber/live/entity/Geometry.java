
package com.example.huber.live.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Geometry {

    @SerializedName("coordinates")
    @Expose
    private List<Double> coordinates = null;

    public Double getLat() {
        return coordinates.get(0);
    }
    public Double getLon() {
        return coordinates.get(1);
    }
}
