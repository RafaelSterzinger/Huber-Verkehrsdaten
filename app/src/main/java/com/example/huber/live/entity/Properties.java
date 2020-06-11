
package com.example.huber.live.entity;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Properties {

    @SerializedName("attributes")
    @Expose
    private Attributes attributes;
    @SerializedName("name")
    @Expose
    private String diva;
    @SerializedName("title")
    @Expose
    private String title;

    public Attributes getAttributes() {
        return attributes;
    }

    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

    public String getDiva() {
        return diva;
    }

    public void setDiva(String diva) {
        this.diva = diva;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
