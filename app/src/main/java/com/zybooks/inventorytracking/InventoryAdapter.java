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

    // Interface for handling item cell clicks
    public interface OnItemClickListener {
        void onItemClick(InventoryItem item);
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

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    // Updates the adapter data and refreshes recyclerview
    public void setItems(List<InventoryItem> items) {
        mItems = items;
        notifyDataSetChanged();
    }

     class ItemViewHolder extends RecyclerView.ViewHolder {
        private final TextView mNameTextView;
        private final TextView mQuantityTextView;
        private final ImageView mItemImageView;



        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            mNameTextView = itemView.findViewById(R.id.item_text_name);
            mQuantityTextView = itemView.findViewById(R.id.item_quantity);
            mItemImageView = itemView.findViewById(R.id.item_image_view);
        }

        // Bind inventoryitem data to viewholder view
        public void bind(InventoryItem item) {
            mNameTextView.setText(item.getName());
            mQuantityTextView.setText(String.valueOf(item.getQuantity()));

            // Display item image if it exists, otherwise uses logo as image
            if (item.getImagePath() != null && !item.getImagePath().isEmpty()) {
                File imgFile = new File(item.getImagePath());
                if (imgFile.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                    mItemImageView.setImageBitmap(bitmap);
                } else {
                    mItemImageView.setImageResource(R.drawable.vector_logo);
                }
            } else {
                mItemImageView.setImageResource(R.drawable.vector_logo);
            }

            // Trigger item click listener if one is registered
            itemView.setOnClickListener(v ->{
                if (mItemClickListener != null) {
                    mItemClickListener.onItemClick(item);
                }
            });
        }
    }
}
