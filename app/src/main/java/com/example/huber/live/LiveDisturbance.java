package com.example.huber.live;

import com.example.huber.live.entity.disturbance.Data;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LiveDisturbance {

    @SerializedName("data")
    @Expose
    private Data data;

    public Data getData() {
        return data;
    }

}
