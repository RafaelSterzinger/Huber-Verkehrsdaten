
package com.example.huber.live.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class DepartureTime {

    @SerializedName("countdown")
    @Expose
    private Integer countdown;
    @SerializedName("timePlanned")
    @Expose
    private String timePlanned;
    @SerializedName("timeReal")
    @Expose
    private String timeReal;

    public Integer getCountdown() {
        return countdown;
    }

    public void setCountdown(Integer countdown) {
        this.countdown = countdown;
    }

    public String getTimePlanned() {
        return timePlanned;
    }

    public void setTimePlanned(String timePlanned) {
        this.timePlanned = timePlanned;
    }

    public String getTimeReal() {
        return timeReal;
    }

    public void setTimeReal(String timeReal) {
        this.timeReal = timeReal;
    }

}
