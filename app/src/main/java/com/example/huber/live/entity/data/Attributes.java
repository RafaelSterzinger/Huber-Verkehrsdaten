package com.example.huber.live.entity.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Attributes {

    @SerializedName("rbl")
    @Expose
    private Integer rbl;

    public Integer getRbl() {
        return rbl;
    }
}
