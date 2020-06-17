package com.example.huber.activity;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import androidx.databinding.DataBindingUtil;

import com.example.huber.MainActivity;
import com.example.huber.R;
import com.example.huber.databinding.DisturbanceItemBinding;
import com.example.huber.live.GetDataService;
import com.example.huber.live.LiveDisturbance;
import com.example.huber.live.RetrofitClientInstance;
import com.example.huber.live.entity.disturbance.TrafficInfo;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DisturbancesActivity extends DrawerActivity {
    List<TrafficInfo> trafficInfoList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.disturbance_title));

        runOnUiThread(() -> requestLiveData((List<TrafficInfo> tiList) -> {
            if (tiList.size() > 0) {
                Log.d("DA", "onCreate:" + tiList);
                LinearLayout scrollView = findViewById(R.id.scrollView);
                scrollView.removeAllViews();
                LayoutInflater inflater = LayoutInflater.from(this);
                trafficInfoList.forEach(trafficInfo -> addEntryToView(scrollView, inflater, trafficInfo));
            }
        }));

    }

    private void addEntryToView(LinearLayout scrollView, LayoutInflater inflater, TrafficInfo trafficInfo) {
        DisturbanceItemBinding binding = DataBindingUtil.inflate(inflater, R.layout.disturbance_item, scrollView, false);    //TODO other entry
        binding.setTrafficInfoVar(trafficInfo);
        View view = binding.getRoot();
        scrollView.addView(view);
    }

    private void requestLiveData(Consumer<List<TrafficInfo>> callback) {
        Log.d(MainActivity.ACTIVITY_NAME, "Requesting live data");

        GetDataService request = RetrofitClientInstance.getRetrofitInstance().create(GetDataService.class);
        request.getLiveDisturbances("stoerunglang", "stoerungkurz").enqueue(new Callback<LiveDisturbance>() {

            @Override
            public void onResponse(@NotNull Call<LiveDisturbance> call, @NotNull Response<LiveDisturbance> response) {
                if (response.body() != null) {
                    trafficInfoList = response.body().getData().getTrafficInfos();
                } else {
                    trafficInfoList = new ArrayList<>();
                }
                callback.accept(trafficInfoList);
            }

            @Override
            public void onFailure(@NotNull Call<LiveDisturbance> call, @NotNull Throwable t) {
                Log.d("Error during API call", t.toString());
                trafficInfoList = new ArrayList<>();
                callback.accept(trafficInfoList);
            }
        });
    }
}
