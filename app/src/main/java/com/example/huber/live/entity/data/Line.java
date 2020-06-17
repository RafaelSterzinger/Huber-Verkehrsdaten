package com.example.huber.live.entity.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Line {

    @SerializedName("departures")
    @Expose
    private Departures departures;
    @SerializedName("direction")
    @Expose
    private String direction;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("towards")
    @Expose
    private String towards;
    @SerializedName("trafficjam")
    @Expose
    private boolean trafficJam;

    public boolean isTrafficJam() {
        return trafficJam;
    }

    public Departures getDepartures() {
        return departures;
    }

    public String getDirection() {
        return direction;
    }

    public String getName() {
        return name;
    }

    public String getTowards() {
        return towards;
    }

}
