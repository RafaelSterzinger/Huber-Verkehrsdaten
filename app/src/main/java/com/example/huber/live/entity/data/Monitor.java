package com.example.huber.live.entity.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.List;

public class Monitor {

    @SerializedName("lines")
    @Expose
    private final List<Line> lines = Collections.emptyList();
    @SerializedName("locationStop")
    @Expose
    private LocationStop locationStop;

    public List<Line> getLines() {
        return lines;
    }

    public boolean linesContainTrafficJam() {
        return lines.stream().anyMatch(Line::isTrafficJam);
    }

    public LocationStop getLocationStop() {
        return locationStop;
    }
}
