package com.ione.smartdict;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DictionaryAdapter extends RecyclerView.Adapter<DictionaryAdapter.WordViewHolder> implements Filterable {

    private Dictionary dictionary;
    private List<String> words;
    private List<String> wordsFiltered;

    public DictionaryAdapter() {
        this.words = new ArrayList<>();
        this.wordsFiltered = new ArrayList<>(words);
    }

    public void setDictionary(Dictionary dictionary) {
        this.dictionary = dictionary;
        notifyDataSetChanged();
    }

    public void addDictionary(Dictionary dictionary) {
        this.dictionary = dictionary;
        notifyDataSetChanged();
    }

    public void setWords(List<String> words) {
        this.words = words;
        this.wordsFiltered = new ArrayList<>(words);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.word_item, parent, false);
        return new WordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
        String word = wordsFiltered.get(position);
        holder.itemTextView.setText(word);
    }

    @Override
    public int getItemCount() {
        return wordsFiltered.size();
    }

    public static class WordViewHolder extends RecyclerView.ViewHolder {

        TextView itemTextView;

        public WordViewHolder(@NonNull View itemView) {
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
                List<String> filteredList = new ArrayList<>();

                if (filterString.isEmpty()) {
                    filteredList.addAll(words);
                } else {
                    try {
                        filteredList = dictionary.search(filterString);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = filteredList;
                return filterResults;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                wordsFiltered.clear();
                wordsFiltered.addAll((List<String>) results.values);
                notifyDataSetChanged();
            }
        };
    }
}

