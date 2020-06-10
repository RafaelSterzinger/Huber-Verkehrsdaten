package com.example.huber.live;


import com.example.huber.entity.LiveEntry;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface GetDataService {

    @Headers({
            "Accept:application/json",
            "Content-Type:application/json"
    })
    @GET("monitor")
    Call<List<LiveEntry>> getStationLiveData(@Query("diva") Integer... diva);
}
