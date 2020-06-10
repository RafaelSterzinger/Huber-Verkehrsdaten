package com.example.huber.live;


import com.example.huber.entity.LiveEntry;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface GetDataService {

    @GET("/monitor")
    Call<List<LiveEntry>> getStationLiveData(@Query("diva") Integer... diva);
}
