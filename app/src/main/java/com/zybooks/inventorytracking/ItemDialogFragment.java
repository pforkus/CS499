package com.zybooks.inventorytracking;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;

import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import android.telephony.SmsManager;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import java.util.Objects;

// Dialog fragment for adding and editing an inventory item
// Operates in two modes: add mode (item is null) and edit mode (item is populated from args)
public class ItemDialogFragment extends DialogFragment {

    private EditText mItemNameEdit;
    private EditText mItemQuantityEdit;
    private ImageView mItemImage;
    private EditText mItemSkuEdit;
    private EditText mItemPriceEdit;
    private EditText mItemCategoryEdit;
    private EditText mItemDetailsEdit;

    private InventoryItem mItem;  // Null for add mode, set for edit mode
    private OnDialogResultListener mListener;
    private String mCurrentImagePath;

    private ImagePickerController mImagePicker;


    public interface OnDialogResultListener {
        void onItemSaved (InventoryItem item, OnActionCompleteCallback callback);
        void onItemDeleted (InventoryItem item, OnActionCompleteCallback callback);
    }

    public interface OnActionCompleteCallback {
        void onComplete(boolean success);
    }
    
    // Passes data as args rather than fields to survive fragment recreation
    public static ItemDialogFragment newInstance(InventoryItem item) {
        ItemDialogFragment fragment = new ItemDialogFragment();
        Bundle args = new Bundle();
        if(item != null) {
            args.putSerializable("item", item);
        }
        fragment.setArguments(args);
        return fragment;
    }
    public void setOnDialogResultListener(OnDialogResultListener listener) {
        mListener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mImagePicker = new ImagePickerController(this, path -> {
            mCurrentImagePath = path;
            ImageStorageHelper.loadImage(path, mItemImage);
        });
    }


    @SuppressLint("ClickableViewAccessibility")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState){
        View view = getLayoutInflater().inflate(R.layout.dialog_item, null);

        mItemNameEdit = view.findViewById(R.id.item_text_name);
        mItemQuantityEdit = view.findViewById(R.id.item_quantity);
        mItemImage = view.findViewById(R.id.item_image);
        mItemSkuEdit = view.findViewById(R.id.item_sku);
        mItemPriceEdit = view.findViewById(R.id.item_price);
        mItemCategoryEdit = view.findViewById(R.id.item_category);
        mItemDetailsEdit = view.findViewById(R.id.item_details);
        Button confirmButton = view.findViewById(R.id.confirm_button);
        Button exitButton = view.findViewById(R.id.exit_button);
        Button incrementButton = view.findViewById(R.id.increment_button);
        Button decrementButton = view.findViewById(R.id.decrement_button);
        Button deleteButton = view.findViewById(R.id.delete_button);

        // Load existing data if in edit mode
        if (getArguments() != null && getArguments().containsKey("item")) {
            mItem = (InventoryItem) getArguments().getSerializable("item");

            // Sets the fields to the UI elements
            mItemNameEdit.setText(Objects.requireNonNull(mItem).getName());
            mItemQuantityEdit.setText(String.valueOf(mItem.getQuantity()));
            mItemSkuEdit.setText(mItem.getSku());
            mItemPriceEdit.setText(mItem.getPrice() != null ? String.valueOf(mItem.getPrice()) : "");
            mItemCategoryEdit.setText(mItem.getCategory());
            mItemDetailsEdit.setText(mItem.getDescription());

            if (mItem.getImageUrl() != null && !mItem.getImageUrl().isEmpty()) {
                mCurrentImagePath = mItem.getImageUrl();
                ImageStorageHelper.loadImage(mCurrentImagePath, mItemImage);
            }

            // Shows delete button in edit mode
            deleteButton.setVisibility(View.VISIBLE);
        } else {
            // Load an empty dialog for adding new items
            mItem = new InventoryItem();
            deleteButton.setVisibility(View.GONE); // Hides delete button in add mode
        }

        // Increment button Click listener
        incrementButton.setOnClickListener(v -> {
            long currentQty = getQuantity();
            mItemQuantityEdit.setText(String.valueOf(currentQty + 1));
        });

        // Decrement button Click listener - floor of 0 to prevent negative quantities
        decrementButton.setOnClickListener(v -> {
            long currentQty = getQuantity();
            if(currentQty > 0) {
                mItemQuantityEdit.setText(String.valueOf(currentQty - 1));
            }
        });

        // Request camera permission if not granted, otherwise show image source dialog
        mItemImage.setOnClickListener(v -> mImagePicker.start());

        // Confirm button
        confirmButton.setOnClickListener(v -> saveItem());

        deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog());


        // Dismiss keyboard when tapping outside an EditText
        view.setOnTouchListener((v, event) -> {
            Dialog dialog = getDialog();
            if (dialog == null) return false;
            View focused = getDialog().getCurrentFocus();
            if (focused instanceof EditText) {
                focused.clearFocus();
                InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(focused.getWindowToken(), 0);
            }
            return false;
        });

        // Pressing exit dismisses dialog
        exitButton.setOnClickListener(v -> dismiss());

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(view);
        return builder.create();

    }

    private void showDeleteConfirmationDialog() {
        // Show confirmation dialog before deleting
        if(mItem != null && mListener != null){
            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Item")
                    .setMessage("Are you sure you want to delete this item?")
                    .setPositiveButton("Delete", (dialog, which) -> mListener.onItemDeleted(mItem, success -> {
                        if(success) {
                            dismiss();
                        } else {
                            Toast.makeText(requireContext(), "Failed to delete item, please try again.", Toast.LENGTH_SHORT).show();
                        }
                    }))
                    .setNegativeButton("Cancel", null)
                    .show();
        }
    }
    // Parses quantity from text field, defaults to 0 if empty or invalid
    private long getQuantity() {
        String qtyStr = mItemQuantityEdit.getText().toString().trim();
        if(qtyStr.isEmpty()) {
            return 0;
        }
        try {
            return Long.parseLong(qtyStr);
        } catch (NumberFormatException e) {
            return 0; // TODO this isnt handled anywhere, silent failure if quantity exceeds 10 digits
        }
    }

    // Validates and sets fields for saved item
    private void saveItem() {

        // Validate required fields- name and quantity
        String name = mItemNameEdit.getText().toString().trim();
        if(name.isEmpty()){
            mItemNameEdit.setError("Name Required");
            return;
        }

        // Checks if quantity field is empty
        String quantityText = mItemQuantityEdit.getText().toString().trim();
        if (quantityText.isEmpty()) {
            mItemQuantityEdit.setError("Quantity Required");
            return;
        }

        // Checks if quantity is valid number
        long quantity;
        try {
            quantity = Long.parseLong(quantityText);
        } catch (NumberFormatException e) {
            mItemQuantityEdit.setError("Invalid quantity");
            return;
        }

        // Trigger SMS alert if item is out of stock
        if(quantity <= 0) {
            sendLowInventoryAlert(name);
        }

        // Set all required fields
        mItem.setName(name);
        mItem.setQuantity(quantity);
        mItem.setImageUrl(mCurrentImagePath);

        // Check if non-required fields are present, otherwise set to null
        String sku = mItemSkuEdit.getText().toString().trim();
        mItem.setSku(sku.isEmpty() ? null : sku);

        String category = mItemCategoryEdit.getText().toString().trim();
        mItem.setCategory(category.isEmpty() ? null : category);

        String description = mItemDetailsEdit.getText().toString().trim();
        mItem.setDescription(description.isEmpty() ? null : description);

        // If save attempt fails, communicate to user, otherwise dismiss dialog
        if (mListener != null) {
            mListener.onItemSaved(mItem, success -> {
                if (success) {
                    dismiss();
                } else {
                    Toast.makeText(requireContext(), "Failed to save item, please try again", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            dismiss();
        }
        
    }

    // Handles SMS inventory alerts, checks permissions and preferences, sends SMS if permissions enabled
    // Returns error if permissions not granted or in case of failure
    private void sendLowInventoryAlert(String itemName) {

        SharedPreferences prefs = requireContext().getSharedPreferences("InventoryPrefs", Context.MODE_PRIVATE);
        boolean alertsEnabled = prefs.getBoolean("sms_alerts_enabled", true);

        if(!alertsEnabled) {
            Log.d("SMS", "SMS permissions disabled by user");
            return;
        }
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.SEND_SMS)
        != PackageManager.PERMISSION_GRANTED) {
            // TODO set up internal notification system, bell icon with alert dialog, listing low inventory? can be simple
            Log.d("SMS", "SMS Permissions have not been granted, skipping alert.");
            return;
        }

        // Retrieve number from shared preferences
        String phoneNumber = prefs.getString("alert_phone_number", null);

        if(phoneNumber == null || phoneNumber.isEmpty()) {
            Log.d("SMS", "No phone number saved, skipping alert");
            return;
        }

        // Create alert message
        String message = "Low Inventory Alert: " + itemName +
                " is out of stock. Please consider replenishing.";

        // Sends the SMS
        try {
            // Send SMS to saved number
            SmsManager smsManager = requireContext().getSystemService(SmsManager.class);
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Log.d("SMS", "SMS sent to " + phoneNumber);
        } catch (Exception e) {
            Log.e("SMS", "Failed to send SMS " + e.getMessage());

        }
    }

}
