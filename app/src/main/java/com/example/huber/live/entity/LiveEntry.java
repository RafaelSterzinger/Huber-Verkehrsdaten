
package com.example.huber.live.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LiveEntry {

    @SerializedName("data")
    @Expose
    private Data data;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }
}
