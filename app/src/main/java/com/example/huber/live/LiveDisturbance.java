
package com.example.huber.live;

import com.example.huber.live.entity.disturbance.Data;
import com.example.huber.live.entity.disturbance.Message;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class LiveDisturbance {

    @SerializedName("data")
    @Expose
    private Data data;
    @SerializedName("message")
    @Expose
    private Message message;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public Message getMessage() {
        return message;
    }

    public void setMessage(Message message) {
        this.message = message;
    }

}
