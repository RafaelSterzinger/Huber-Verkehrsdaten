
package com.example.huber.live.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Departure {

    @SerializedName("departureTime")
    @Expose
    private DepartureTime departureTime;

    public DepartureTime getDepartureTime() {
        return departureTime;
    }

    public void setDepartureTime(DepartureTime departureTime) {
        this.departureTime = departureTime;
    }

}
