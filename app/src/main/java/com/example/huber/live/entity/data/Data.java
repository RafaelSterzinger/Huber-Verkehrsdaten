package com.example.huber.live.entity.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.List;

public class Data {

    @SerializedName("monitors")
    @Expose
    private final List<Monitor> monitors = Collections.emptyList();

    public List<Monitor> getMonitors() {
        return monitors;
    }

}
