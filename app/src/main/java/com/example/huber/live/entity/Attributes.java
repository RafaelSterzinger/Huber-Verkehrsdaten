
package com.example.huber.live.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Attributes {

    @SerializedName("rbl")
    @Expose
    private Integer rbl;

    public Integer getRbl() {
        return rbl;
    }

    public void setRbl(Integer rbl) {
        this.rbl = rbl;
    }

}
