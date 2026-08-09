package com.zybooks.inventorytracking;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;

import android.net.Uri;
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

import java.util.Locale;
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
    private Button mConfirmButton;
    private Button mExitButton;
    private Uri mLocalImageUri;
    private String mCurrentImageUrl;
    private InventoryItem mItem;  // Null for add mode, set for edit mode
    private OnDialogResultListener mListener;
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

        mImagePicker = new ImagePickerController(this, new ImagePickerController.OnImageReadyListener() {
            @Override
            public void onImagePreviewReady(Uri localUri) {
                Log.d("ImageFlow", "onImagePreviewReady called with: " + localUri);
                mLocalImageUri = localUri;
                ImageStorageHelper.loadImage(localUri, mItemImage);
                Log.d("ImageFlow", "loadImage called, mItemImage = " + mItemImage);
            }
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
        mConfirmButton = view.findViewById(R.id.confirm_button);
        mExitButton = view.findViewById(R.id.exit_button);

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
            mItemPriceEdit.setText(String.format(Locale.US, "%.2f", mItem.getPrice())); // Formatted to avoid trimmed trailing zeroes
            mItemCategoryEdit.setText(mItem.getCategory());
            mItemDetailsEdit.setText(mItem.getDescription());

            if (mItem.getImageUrl() != null && !mItem.getImageUrl().isEmpty()) {
                mCurrentImageUrl = mItem.getImageUrl();
                ImageStorageHelper.loadImage(mCurrentImageUrl, mItemImage);
            }

            // Shows delete button in edit mode
            deleteButton.setVisibility(View.VISIBLE);
        } else { // In Add mode, we do not need to populate the fields or display a delete button
            mItem = new InventoryItem();
            deleteButton.setVisibility(View.INVISIBLE);
        }

        // Increment button click listener
        incrementButton.setOnClickListener(v -> {
            long currentQty = getQuantity();
            mItemQuantityEdit.setText(String.valueOf(currentQty + 1));
        });

        // Decrement button click listener - floor of 0 to prevent negative quantities
        decrementButton.setOnClickListener(v -> {
            long currentQty = getQuantity();
            if(currentQty > 0) {
                mItemQuantityEdit.setText(String.valueOf(currentQty - 1));
            }
        });

        // Request camera permission if not granted, otherwise show image source dialog
        mItemImage.setOnClickListener(v -> mImagePicker.start());

        // Sets confirm button to save item
        mConfirmButton.setOnClickListener(v -> saveItem());

        // Sets delete button to open delete confirmation window
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
        mExitButton.setOnClickListener(v -> dismiss());

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
            return 0;
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
        mItem.setImageUrl(mCurrentImageUrl);

        // For non-required fields, set if present, otherwise set as null
        String sku = mItemSkuEdit.getText().toString().trim();
        mItem.setSku(sku.isEmpty() ? null : sku);

        String category = mItemCategoryEdit.getText().toString().trim();
        mItem.setCategory(category.isEmpty() ? null : category);

        String description = mItemDetailsEdit.getText().toString().trim();
        mItem.setDescription(description.isEmpty() ? null : description);

        String price = mItemPriceEdit.getText().toString().trim();
        try {
            mItem.setPrice(price.isEmpty() ? null : Double.parseDouble(price));
        }  catch (NumberFormatException e) {
            Toast.makeText(requireContext(), "Please enter a valid price", Toast.LENGTH_SHORT).show();
            return;
        }

        if (mLocalImageUri != null) {
            mConfirmButton.setEnabled(false);
            mExitButton.setEnabled(false);
            setCancelable(false);

            ImageStorageHelper.uploadImage(mLocalImageUri, new ImageStorageHelper.UploadCallback() {
                @Override
                public void onStart() { }

                @Override
                public void onSuccess(String secureUrl, String publicId) {
                    mCurrentImageUrl = secureUrl;
                    mItem.setImageUrl(mCurrentImageUrl);
                    mItem.setImagePublicId(publicId);
                    finishSave();
                }

                @Override
                public void onError(String message) {
                    Toast.makeText(requireContext(), "Upload failed: " + message, Toast.LENGTH_SHORT).show();
                    mConfirmButton.setEnabled(true);
                    mExitButton.setEnabled(true);
                    setCancelable(true);
                }
            });
        } else {
            mItem.setImageUrl(mCurrentImageUrl);
            finishSave();
        }
    }


    private void finishSave() {
        if (mListener != null) {
            mListener.onItemSaved(mItem, success -> {
                if (success) {
                    dismiss();
                } else {
                    Toast.makeText(requireContext(), "Failed to save item, please try again", Toast.LENGTH_SHORT).show();
                    mConfirmButton.setEnabled(true);
                    mExitButton.setEnabled(true);
                    setCancelable(true);
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
