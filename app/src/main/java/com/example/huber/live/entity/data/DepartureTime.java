package com.example.huber.live.entity.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DepartureTime {

    @SerializedName("countdown")
    @Expose
    private Integer countdown;

    public Integer getCountdown() {
        return countdown;
    }
}
