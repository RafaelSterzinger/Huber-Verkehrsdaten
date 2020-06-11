package com.example.huber.util;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.huber.R;
import com.example.huber.entity.Station;
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter;

import org.jetbrains.annotations.NotNull;

import java.util.List;


// https://github.com/mancj/MaterialSearchBar/wiki/Custom-Suggestions-Adapter
public class CustomSuggestionsAdapter extends SuggestionsAdapter<Station, CustomSuggestionsAdapter.SuggestionHolder> {

    public CustomSuggestionsAdapter(LayoutInflater inflater) {
        super(inflater);
    }

    @Override
    public int getSingleViewHeight() {
        return 40;
    }

    @NotNull
    @Override
    public SuggestionHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        View view = getLayoutInflater().inflate(R.layout.station_suggestion, parent, false);
        return new SuggestionHolder(view);
    }

    @Override
    public void setSuggestions(List<Station> suggestions) {
        super.setSuggestions(suggestions);
    }

    @Override
    public void onBindSuggestionHolder(Station suggestion, SuggestionHolder holder, int position) {
        holder.title.setText(suggestion.getName());
        holder.title.setId(suggestion.getUid());
    }

    public static class SuggestionHolder extends RecyclerView.ViewHolder {
        private TextView title;

        private SuggestionHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
        }
    }

}