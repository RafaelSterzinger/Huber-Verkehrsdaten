package com.example.huber.live;


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
    Call<LiveData> getStationLiveData(@Query("diva") Integer... diva);

    @Headers({
            "Accept:application/json",
            "Content-Type:application/json"
    })
    @GET("trafficInfoList")
    Call<LiveDisturbance> getLiveDisturbances(@Query("name") String... name);
}
