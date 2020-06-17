
package com.example.huber.live.entity.disturbance;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Attributes {

    @SerializedName("reason")
    @Expose
    private String reason;
    @SerializedName("relatedLines")
    @Expose
    private List<String> relatedLines = null;
    @SerializedName("station")
    @Expose
    private String station;
    @SerializedName("location")
    @Expose
    private String location;
    @SerializedName("towards")
    @Expose
    private String towards;
    @SerializedName("relatedStops")
    @Expose
    private List<Integer> relatedStops = null;
    @SerializedName("status")
    @Expose
    private String status;
    @SerializedName("ausBis")
    @Expose
    private String ausBis;
    @SerializedName("ausVon")
    @Expose
    private String ausVon;
    @SerializedName("relatedLineTypes")
    @Expose
    private RelatedLineTypes relatedLineTypes;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public List<String> getRelatedLines() {
        return relatedLines;
    }

    public void setRelatedLines(List<String> relatedLines) {
        this.relatedLines = relatedLines;
    }

    public String getStation() {
        return station;
    }

    public void setStation(String station) {
        this.station = station;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getTowards() {
        return towards;
    }

    public void setTowards(String towards) {
        this.towards = towards;
    }

    public List<Integer> getRelatedStops() {
        return relatedStops;
    }

    public void setRelatedStops(List<Integer> relatedStops) {
        this.relatedStops = relatedStops;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getAusBis() {
        return ausBis;
    }

    public void setAusBis(String ausBis) {
        this.ausBis = ausBis;
    }

    public String getAusVon() {
        return ausVon;
    }

    public void setAusVon(String ausVon) {
        this.ausVon = ausVon;
    }

    public RelatedLineTypes getRelatedLineTypes() {
        return relatedLineTypes;
    }

    public void setRelatedLineTypes(RelatedLineTypes relatedLineTypes) {
        this.relatedLineTypes = relatedLineTypes;
    }

}
