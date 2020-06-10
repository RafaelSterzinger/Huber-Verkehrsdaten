
package com.example.huber.live.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Monitor {

    @SerializedName("lines")
    @Expose
    private List<Line> lines = null;
    @SerializedName("locationStop")
    @Expose
    private LocationStop locationStop;

    public List<Line> getLines() {
        return lines;
    }

    public void setLines(List<Line> lines) {
        this.lines = lines;
    }

    public LocationStop getLocationStop() {
        return locationStop;
    }

    public void setLocationStop(LocationStop locationStop) {
        this.locationStop = locationStop;
    }

}
