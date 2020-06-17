package com.example.huber.live.entity.disturbance;

import android.text.TextUtils;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.List;

public class TrafficInfo {

    @SerializedName("relatedStops")
    @Expose
    private final List<String> relatedLines = Collections.emptyList();
    @SerializedName("relatedStops")
    @Expose
    private final List<Integer> relatedStops = Collections.emptyList();
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("time")
    @Expose
    private Time time;

    public String getRelatedLinesString() {
        return TextUtils.join("|", relatedLines.subList(0, Math.min(3, relatedLines.size())));
    }

    public String getRelatedStopsString() {
        return TextUtils.join("|", relatedStops.subList(0, Math.min(3, relatedStops.size())));
    }

    public String getTimeString() {
        String timeStr = "";
        if (!time.getStart().isEmpty()) {
            timeStr += time.getStart().substring(0, 16).replace('T', ' ');
        }
        if (!time.getEnd().isEmpty()) {
            if (timeStr.length() > 0) {
                timeStr += " – ";
            }
            timeStr += time.getEnd().substring(0, 16).replace('T', ' ');
        }
        return timeStr;
    }

    public String getName() {
        return name;
    }

    public String getTitle() {
        return title;
    }


    public String getDescription() {
        return description;
    }

}
