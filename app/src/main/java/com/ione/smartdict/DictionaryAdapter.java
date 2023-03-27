package com.ione.smartdict;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.List;

public class DictionaryAdapter extends RecyclerView.Adapter<DictionaryAdapter.DictionaryViewHolder> implements Filterable {

    private List<Dictionary> dictionaries;
    private List<Dictionary> dictionariesFiltered;

    public DictionaryAdapter() {
        this.dictionaries = new ArrayList<>();
        this.dictionariesFiltered = new ArrayList<>(dictionaries);
    }

    public void addDictionary(Dictionary dictionary) {
        dictionaries.add(dictionary);
        dictionariesFiltered.add(dictionary);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public DictionaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dictionary_item, parent, false);
        return new DictionaryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DictionaryViewHolder holder, int position) {
        Dictionary dictionary = dictionariesFiltered.get(position);
        String dictionaryName = dictionary.getDictionaryName();
        holder.itemTextView.setText(dictionaryName);
    }

    @Override
    public int getItemCount() {
        return dictionariesFiltered.size();
    }

    public static class DictionaryViewHolder extends RecyclerView.ViewHolder {

        TextView itemTextView;

        public DictionaryViewHolder(@NonNull View itemView) {
            super(itemView);
            itemTextView = itemView.findViewById(R.id.item_text_view);
        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String filterString = constraint.toString().toLowerCase().trim();
                List<Dictionary> filteredList = new ArrayList<>();

                if (filterString.isEmpty()) {
                    filteredList.addAll(dictionaries);
                } else {
                    for (Dictionary dictionary : dictionaries) {
                        if (dictionary.getDictionaryName().toLowerCase().contains(filterString)) {
                            filteredList.add(dictionary);
                        }
                    }
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredList;
                return filterResults;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                dictionariesFiltered.clear();
                dictionariesFiltered.addAll((List<Dictionary>) results.values);
                notifyDataSetChanged();
            }
        };
    }
}

