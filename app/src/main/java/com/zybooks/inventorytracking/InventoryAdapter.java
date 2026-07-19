package com.zybooks.inventorytracking;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import java.io.File;
import java.util.List;
import java.util.ArrayList;

// Adapter for displaying inventory items in a RecyclerView
public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.ItemViewHolder> {

    private List<InventoryItem> mItems = new ArrayList<>();
    private OnItemClickListener mItemClickListener;
    private OnSelectionStateChangedListener mSelectionStateChangedListener;

    // Multiselect state
    public final SelectionTracker<String> selection = new SelectionTracker<>();

    // Interface for handling individual item cell clicks
    public interface OnItemClickListener {
        void onItemClick(InventoryItem item);
    }

    // Interface for notifying host that selection has changed
    public interface OnSelectionStateChangedListener {
        void onSelectionChanged();
    }
    public void setOnSelectionStateChangedListener(OnSelectionStateChangedListener listener) {
        mSelectionStateChangedListener = listener;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mItemClickListener = listener;
    }

    // Inflates the item layout and returns new viewholder
    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_view_items, parent, false);
        return new ItemViewHolder(view);
    }

    // Binds item data to the viewholder at the given position
    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        InventoryItem item = mItems.get(position);
        holder.bind(item);
    }

    // Returns the size of the items array
    @Override
    public int getItemCount() {
        return mItems.size();
    }

    // Updates the adapter data and refreshes recyclerview
    public void setItems(List<InventoryItem> items) {
        mItems = items;
        notifyDataSetChanged(); // FIXME seek more efficient alternatives
    }

    // ==========
    // Multi - Select
    // ==========

    // Returns the  currently selected items for the host to act on
    public List<InventoryItem> getSelectedItems() {
        List<InventoryItem> result = new ArrayList<>();
        for (InventoryItem item: mItems) {
            if(selection.isSelected(item.getId())) {
                result.add(item);
            }
        }
        return result;
     }

     private void toggleSelection(InventoryItem item, int position) {

        selection.toggle(item.getId());

        // Rebinds item so selected items are shown as such
        notifyItemChanged(position);

        if(mSelectionStateChangedListener != null) {
            mSelectionStateChangedListener.onSelectionChanged();
        }

        // When selection mode ends, rebind all rows
        if(!selection.isActive()) {
            notifyDataSetChanged(); // FIXME seek more efficient alternatives
        }
     }

     public void clearSelection() {
        selection.clear();
        notifyDataSetChanged(); // FIXME seek more efficient alternatives (i.e: DiffUtil, NotifyItemRangedChanged)
     }

     public class ItemViewHolder extends RecyclerView.ViewHolder {
        private final TextView mNameTextView;
        private final TextView mQuantityTextView;
        private final ImageView mItemImageView;



        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            mNameTextView = itemView.findViewById(R.id.item_text_name);
            mQuantityTextView = itemView.findViewById(R.id.item_quantity);
            mItemImageView = itemView.findViewById(R.id.item_image_view);
        }

        // Bind inventory item data to view holder view
        public void bind(InventoryItem item) {
            mNameTextView.setText(item.getName());
            mQuantityTextView.setText(String.valueOf(item.getQuantity()));

            // FIXME With new database, this is still saving locally, need Glide or equivalent to store externally, so other mobile apps can retrieve images
            // Display item image if it exists, otherwise uses logo as image
            if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                File imgFile = new File(item.getImageUrl());
                if (imgFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    mItemImageView.setImageBitmap(bitmap);
                } else {
                    mItemImageView.setImageResource(R.drawable.vector_logo);
                }
            } else {
                mItemImageView.setImageResource(R.drawable.vector_logo);
            }

            // Tracks Multi-select UI state
            boolean isSelected = selection.isSelected(item.getId());
            itemView.setActivated(isSelected);



            // Tap: toggle selection if in selection ode, otherwise opens item dialog
            itemView.setOnClickListener(v ->{
                int position = getBindingAdapterPosition();
                if(position == RecyclerView.NO_POSITION) return;

                if (selection.isActive()) {
                    toggleSelection(item, position);

                } else if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(item);
                }
            });

            // Long-press: enter selection mode
            itemView.setOnLongClickListener(v -> {
                int position = getBindingAdapterPosition();
                if(position == RecyclerView.NO_POSITION) return false;

                if(!selection.isActive()) {
                    toggleSelection(item, getBindingAdapterPosition());
                }
                return true;
            });
        }
    }
}
