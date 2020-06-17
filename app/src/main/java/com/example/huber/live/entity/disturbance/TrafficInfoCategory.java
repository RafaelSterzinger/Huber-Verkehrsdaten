
package com.example.huber.live.entity.disturbance;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class TrafficInfoCategory {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("refTrafficInfoCategoryGroupId")
    @Expose
    private Integer refTrafficInfoCategoryGroupId;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("trafficInfoNameList")
    @Expose
    private String trafficInfoNameList;
    @SerializedName("title")
    @Expose
    private String title;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getRefTrafficInfoCategoryGroupId() {
        return refTrafficInfoCategoryGroupId;
    }

    public void setRefTrafficInfoCategoryGroupId(Integer refTrafficInfoCategoryGroupId) {
        this.refTrafficInfoCategoryGroupId = refTrafficInfoCategoryGroupId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTrafficInfoNameList() {
        return trafficInfoNameList;
    }

    public void setTrafficInfoNameList(String trafficInfoNameList) {
        this.trafficInfoNameList = trafficInfoNameList;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
