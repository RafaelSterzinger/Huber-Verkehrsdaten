
package com.example.huber.live.entity.disturbance;

import java.util.List;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Data {

    @SerializedName("trafficInfos")
    @Expose
    private List<TrafficInfo> trafficInfos = null;
    @SerializedName("trafficInfoCategories")
    @Expose
    private List<TrafficInfoCategory> trafficInfoCategories = null;
    @SerializedName("trafficInfoCategoryGroups")
    @Expose
    private List<TrafficInfoCategoryGroup> trafficInfoCategoryGroups = null;

    public List<TrafficInfo> getTrafficInfos() {
        return trafficInfos;
    }

    public void setTrafficInfos(List<TrafficInfo> trafficInfos) {
        this.trafficInfos = trafficInfos;
    }

    public List<TrafficInfoCategory> getTrafficInfoCategories() {
        return trafficInfoCategories;
    }

    public void setTrafficInfoCategories(List<TrafficInfoCategory> trafficInfoCategories) {
        this.trafficInfoCategories = trafficInfoCategories;
    }

    public List<TrafficInfoCategoryGroup> getTrafficInfoCategoryGroups() {
        return trafficInfoCategoryGroups;
    }

    public void setTrafficInfoCategoryGroups(List<TrafficInfoCategoryGroup> trafficInfoCategoryGroups) {
        this.trafficInfoCategoryGroups = trafficInfoCategoryGroups;
    }

}
