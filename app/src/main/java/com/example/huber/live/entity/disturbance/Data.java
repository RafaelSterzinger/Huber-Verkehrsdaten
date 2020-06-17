package com.example.huber.live.entity.disturbance;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.List;

public class Data {

    @SerializedName("trafficInfos")
    @Expose
    private final List<TrafficInfo> trafficInfos = Collections.emptyList();

    public List<TrafficInfo> getTrafficInfos() {
        return trafficInfos;
    }
}
