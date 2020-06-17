
package com.example.huber.live.entity.disturbance;

import android.text.TextUtils;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TrafficInfo {

    @SerializedName("refTrafficInfoCategoryId")
    @Expose
    private Integer refTrafficInfoCategoryId;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("attributes")
    @Expose
    private Attributes attributes;
    @SerializedName("relatedLines")
    @Expose
    private List<String> relatedLines = null;
    @SerializedName("relatedStops")
    @Expose
    private List<Integer> relatedStops = null;
    @SerializedName("time")
    @Expose
    private Time time;
    @SerializedName("priority")
    @Expose
    private String priority;
    @SerializedName("owner")
    @Expose
    private String owner;

    public String getRelatedLinesString() {
        return (relatedLines != null) ? TextUtils.join("|", relatedLines.subList(0, Math.min(3, relatedLines.size()))) : "";
    }
    public String getRelatedStopsString() {
        return (relatedStops != null) ? TextUtils.join("|", relatedStops.subList(0, Math.min(3, relatedStops.size()))) : "";
    }

    public String getTimeString() {
        String timeStr = "";
        if (! time.getStart().isEmpty()){
            timeStr += time.getStart().substring(0, 16).replace('T', ' ');
        }
        if (! time.getEnd().isEmpty()){
            if (timeStr.length() > 0) {
                timeStr += " – ";
            }
            timeStr += time.getEnd().substring(0, 16).replace('T', ' ');
        }
    return timeStr;
    }

    public Integer getRefTrafficInfoCategoryId() {
        return refTrafficInfoCategoryId;
    }

    public void setRefTrafficInfoCategoryId(Integer refTrafficInfoCategoryId) {
        this.refTrafficInfoCategoryId = refTrafficInfoCategoryId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Attributes getAttributes() {
        return attributes;
    }

    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

    public List<String> getRelatedLines() {
        return relatedLines;
    }

    public void setRelatedLines(List<String> relatedLines) {
        this.relatedLines = relatedLines;
    }

    public List<Integer> getRelatedStops() {
        return relatedStops;
    }

    public void setRelatedStops(List<Integer> relatedStops) {
        this.relatedStops = relatedStops;
    }

    public Time getTime() {
        return time;
    }

    public void setTime(Time time) {
        this.time = time;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

}
