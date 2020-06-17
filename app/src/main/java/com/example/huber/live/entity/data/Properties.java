package com.example.huber.live.entity.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Properties {

    @SerializedName("attributes")
    @Expose
    private Attributes attributes;
    @SerializedName("name")
    @Expose
    private String diva;

    public Attributes getAttributes() {
        return attributes;
    }

    public String getDiva() {
        return diva;
    }
}
