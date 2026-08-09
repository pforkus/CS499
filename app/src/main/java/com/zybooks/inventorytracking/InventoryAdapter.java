package com.zybooks.inventorytracking;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import java.text.NumberFormat;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;

// Adapter for displaying inventory items in a RecyclerView
public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.ItemViewHolder> {

    private List<InventoryItem> mItems = new ArrayList<>();
    private OnItemClickListener mItemClickListener;
    private OnSelectionStateChangedListener mSelectionStateChangedListener;
    private static final int VIEW_TYPE_GRID = 0;
    private static final int VIEW_TYPE_LIST = 1;
    private boolean mIsGridView = true;

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
        int layoutId = (viewType == VIEW_TYPE_GRID)
                ? R.layout.recycler_view_items
                : R.layout.recycler_view_items_list;

        View view = LayoutInflater.from(parent.getContext())
                .inflate(layoutId, parent, false);
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
    public void setItems(List<InventoryItem> newItems) {
        ItemDiffCallback callback = new ItemDiffCallback(mItems, newItems);
        DiffUtil.DiffResult result = DiffUtil.calculateDiff(callback);
        mItems = newItems;
        result.dispatchUpdatesTo(this);
    }

    public void setGridView(boolean isGridView) {
        mIsGridView = isGridView;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return mIsGridView ? VIEW_TYPE_GRID : VIEW_TYPE_LIST;
    }

    // ==========
    // Multi - Select
    // ==========

    // Adds currently selected items' ids to a list and returns them for host actions
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
        notifyItemChanged(position);

        if(mSelectionStateChangedListener != null) {
            mSelectionStateChangedListener.onSelectionChanged();
        }
     }

     public void clearSelection() {
        // Create a snapshot of selected items ids
        Set<String> previouslySelected = selection.selectedIds();

        selection.clear();
        for(int i = 0; i < mItems.size(); i++) {
            if(previouslySelected.contains(mItems.get(i).getId())) {
                notifyItemChanged(i);
            }
        }
     }

     public class ItemViewHolder extends RecyclerView.ViewHolder {
        private final TextView mNameTextView;
        private final TextView mQuantityTextView;
        private final ImageView mItemImageView;
        private final TextView mCategoryTextView;
        private final TextView mPriceTextView;



        ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            mNameTextView = itemView.findViewById(R.id.item_text_name);
            mQuantityTextView = itemView.findViewById(R.id.item_quantity);
            mItemImageView = itemView.findViewById(R.id.item_image_view);
            mCategoryTextView = itemView.findViewById(R.id.item_category_value);
            mPriceTextView = itemView.findViewById(R.id.item_price_value);
        }

        // Bind inventory item data to view holder view
        public void bind(InventoryItem item) {
            mNameTextView.setText(item.getName());

            // Simplifies big quantity's formats to avoid ellipsizes hogging card space
            mQuantityTextView.setText(item.getQuantity() > 999
                    ? "999+" : String.valueOf(item.getQuantity()));


            if(mCategoryTextView != null) {
                mCategoryTextView.setText(item.getCategory());
            }
            if (mPriceTextView != null) {
                if (item.getPrice() == null) {
                    mPriceTextView.setText("");
                } else if (item.getPrice() > 9999.99) {
                    mPriceTextView.setText(R.string.largePricePlaceHolder);
                } else {
                    mPriceTextView.setText(formatPrice(item.getPrice()));
                }
            }

            if(mItemImageView != null) {
                // Display item image if it exists, otherwise uses logo as image
                if (item.getImageUrl() != null && !item.getImageUrl().isEmpty()) {
                    ImageStorageHelper.loadImage(item.getImageUrl(), mItemImageView);
                } else {
                    mItemImageView.setImageResource(R.drawable.vector_logo);
                }
            }


            // Tracks Multi-select UI state
            boolean isSelected = selection.isSelected(item.getId());
            itemView.setActivated(isSelected);



            // Tap: toggle selection if in selection mode, otherwise opens item dialog
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

        private String formatPrice(Double price) {
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.getDefault());
            return currencyFormat.format(price);
        }
    }
}
