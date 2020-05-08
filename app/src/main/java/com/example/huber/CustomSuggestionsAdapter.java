package com.example.huber;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.huber.entity.Station;
import com.mancj.materialsearchbar.adapter.SuggestionsAdapter;

import java.util.ArrayList;


// see docu https://github.com/mancj/MaterialSearchBar/wiki/Custom-Suggestions-Adapter
public class CustomSuggestionsAdapter extends SuggestionsAdapter<Station, CustomSuggestionsAdapter.SuggestionHolder> {

    public CustomSuggestionsAdapter(LayoutInflater inflater) {
        super(inflater);
    }

    @Override
    public int getSingleViewHeight() {
        return 80;
    }

    @Override
    public SuggestionHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = getLayoutInflater().inflate(R.layout.station_suggestion, parent, false);
        return new SuggestionHolder(view);
    }

    @Override
    public void onBindSuggestionHolder(Station suggestion, SuggestionHolder holder, int position) {
        holder.title.setText(suggestion.getName());
        //holder.subtitle.setText("The price is " + suggestion.getPrice() + "$");
    }

    /**
     * <b>Override to customize functionality</b>
     * <p>Returns a filter that can be used to constrain data with a filtering
     * pattern.</p>
     * <p>
     * <p>This method is usually implemented by {@link androidx.recyclerview.widget.RecyclerView.Adapter}
     * classes.</p>
     *
     * @return a filter used to constrain data
     */
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                String term = constraint.toString();
                if(term.isEmpty())
                    suggestions = suggestions_clone;
                else {
                    suggestions = new ArrayList<>();
                    for (Station station: suggestions_clone)
                        if(station.getName().toLowerCase().contains(term.toLowerCase()))
                            suggestions.add(station);
                }
                results.values = suggestions;
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                suggestions = (ArrayList<Station>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    static class SuggestionHolder extends RecyclerView.ViewHolder{
        protected TextView title;
        //protected TextView subtitle;
        //protected ImageView image;

        public SuggestionHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            //subtitle = (TextView) itemView.findViewById(R.id.subtitle);
        }
    }

}