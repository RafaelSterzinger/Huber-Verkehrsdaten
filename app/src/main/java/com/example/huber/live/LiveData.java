
package com.example.huber.live;

import com.example.huber.live.entity.Data;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LiveData {

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
