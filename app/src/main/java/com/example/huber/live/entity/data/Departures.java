package com.example.huber.live.entity.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.List;

public class Departures {

    @SerializedName("departure")
    @Expose
    private final List<Departure> departure = Collections.emptyList();

    public List<Departure> getDeparture() {
        return departure;
    }

}
