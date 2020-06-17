
package com.example.huber.live.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Line {

    @SerializedName("departures")
    @Expose
    private Departures departures;
    @SerializedName("direction")
    @Expose
    private String direction;
    @SerializedName("lineId")
    @Expose
    private Integer lineId;
    @SerializedName("name")
    @Expose
    private String name;
   @SerializedName("realtimeSupported")
    @Expose
    private Boolean realtimeSupported;
    @SerializedName("richtungsId")
    @Expose
    private String richtungsId;
    @SerializedName("towards")
    @Expose
    private String towards;
   @SerializedName("type")
    @Expose
    private String type;

    public boolean isTrafficjam() {
        return trafficjam;
    }

    public void setTrafficjam(boolean trafficjam) {
        this.trafficjam = trafficjam;
    }

    @SerializedName("trafficjam")
    @Expose
    private boolean trafficjam;

    public Departures getDepartures() {
        return departures;
    }

    public void setDepartures(Departures departures) {
        this.departures = departures;
    }

    public String getDirection() {
        return direction;
    }

    public void setDirection(String direction) {
        this.direction = direction;
    }

    public Integer getLineId() {
        return lineId;
    }

    public void setLineId(Integer lineId) {
        this.lineId = lineId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Boolean getRealtimeSupported() {
        return realtimeSupported;
    }

    public void setRealtimeSupported(Boolean realtimeSupported) {
        this.realtimeSupported = realtimeSupported;
    }

    public String getRichtungsId() {
        return richtungsId;
    }

    public void setRichtungsId(String richtungsId) {
        this.richtungsId = richtungsId;
    }

    public String getTowards() {
        return towards;
    }

    public void setTowards(String towards) {
        this.towards = towards;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
