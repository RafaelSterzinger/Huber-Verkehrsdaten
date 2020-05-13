package com.example.huber;

import android.os.AsyncTask;

import com.example.huber.database.HuberDataBase;
import com.example.huber.entity.Station;

import java.util.List;

public class FilterStopsTask extends AsyncTask<CharSequence, Integer, List<Station>> {

    private HuberDataBase dataBase;
    private CustomSuggestionsAdapter adapter;

    FilterStopsTask(HuberDataBase dataBase, CustomSuggestionsAdapter adapter) {
        this.dataBase = dataBase;
        this.adapter = adapter;
    }

    @Override
    protected List<Station> doInBackground(CharSequence... charSequences) {
        String query = "%" + charSequences[0] + "%";
        return this.dataBase.stationDao().getStationsWithNameLike(query);
    }

    @Override
    protected void onPostExecute(List<Station> stations) {
        this.adapter.setSuggestions(stations);
        super.onPostExecute(stations);
    }
}
