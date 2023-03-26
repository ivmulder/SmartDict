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

    private List<String> dictionaryItems;
    private List<String> dictionaryItemsFiltered;

    public DictionaryAdapter(List<String> dictionaryItems) {
        this.dictionaryItems = dictionaryItems;
        this.dictionaryItemsFiltered = new ArrayList<>(dictionaryItems);
    }

    @NonNull
    @Override
    public DictionaryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.dictionary_item, parent, false);
        return new DictionaryViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull DictionaryViewHolder holder, int position) {
        String item = dictionaryItemsFiltered.get(position);
        holder.itemTextView.setText(item);
    }

    @Override
    public int getItemCount() {
        return dictionaryItemsFiltered.size();
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
                List<String> filteredList = new ArrayList<>();

                if (filterString.isEmpty()) {
                    filteredList.addAll(dictionaryItems);
                } else {
                    for (String item : dictionaryItems) {
                        if (item.toLowerCase().contains(filterString)) {
                            filteredList.add(item);
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
                dictionaryItemsFiltered.clear();
                dictionaryItemsFiltered.addAll((List<String>) results.values);
                notifyDataSetChanged();
            }
        };
    }
}
