package com.zybooks.inventorytracking;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class SortBottomSheet extends BottomSheetDialogFragment {

    private InventoryViewModel mViewModel;
    private SortField selectedSort = SortField.NAME;
    private String selectedOrder = "asc";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottomsheet_sort, container, false);

        mViewModel = new ViewModelProvider(requireActivity())
                .get(InventoryViewModel.class);


        RadioGroup sortGroup = view.findViewById(R.id.sortFieldGroup);
        RadioGroup orderGroup = view.findViewById(R.id.sortDirectionGroup);
        Button applyButton = view.findViewById(R.id.applySortButton);


        // Click listener for user sort selection fields
        sortGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if(checkedId == R.id.sort_name) {
                selectedSort = SortField.NAME;
            }
            else if (checkedId == R.id.sort_category) {
                selectedSort = SortField.CATEGORY;
            }
            else if (checkedId == R.id.sort_date) {
                selectedSort = SortField.CREATED;
            }
            else if (checkedId == R.id.sort_price) {
                selectedSort = SortField.PRICE;
            }
        });

        // Click listener for user sort order options
        orderGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.sort_asc) {
                selectedOrder = "asc";
            } else if (checkedId == R.id.sort_desc) {
                selectedOrder = "desc";
            }
        });

        // On button press, applies selected sort fields and order
        applyButton.setOnClickListener(v -> {
            mViewModel.sort(
                    selectedSort.getApiValue(),
                    selectedOrder
            );
            dismiss();
        });

        return view;
    }
}