package com.example.huber.task;

import android.os.AsyncTask;

import com.example.huber.util.CustomSuggestionsAdapter;
import com.example.huber.database.HuberDataBase;
import com.example.huber.entity.Station;

import java.util.List;

public class FilterStopsTask extends AsyncTask<CharSequence, Integer, List<Station>> {

    private final HuberDataBase dataBase;
    private final CustomSuggestionsAdapter adapter;
    private final Runnable callback;

    public FilterStopsTask(HuberDataBase dataBase, CustomSuggestionsAdapter adapter, Runnable callback) {
        this.dataBase = dataBase;
        this.adapter = adapter;
        this.callback = callback;
    }

    @Override
    protected List<Station> doInBackground(CharSequence... charSequences) {
        String query = "%" + charSequences[0] + "%";
        return this.dataBase.stationDao().getStationsWithNameLike(query);
    }

    @Override
    protected void onPostExecute(List<Station> stations) {
        this.adapter.setSuggestions(stations);
        callback.run();
        super.onPostExecute(stations);
    }
}
