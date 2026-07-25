package com.zybooks.inventorytracking;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SuggestionAdapter extends RecyclerView.Adapter<SuggestionAdapter.ViewHolder> {

    public interface OnSuggestionClickListener {
        void onSuggestionClick(String name);
    }

    private List<String> mSuggestions = new ArrayList<>();
    private final OnSuggestionClickListener mListener;

    public SuggestionAdapter(OnSuggestionClickListener listener) {
        mListener = listener;
    }

    public void submitList(List<String> suggestions) {
        mSuggestions = suggestions;
        notifyDataSetChanged(); // FIXME
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String name = mSuggestions.get(position);
        holder.textView.setText(name);
        holder.itemView.setOnClickListener(v -> mListener.onSuggestionClick(name));
    }

    @Override
    public int getItemCount() {
        return mSuggestions.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}
