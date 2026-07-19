package com.zybooks.inventorytracking;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Matrix;
import androidx.exifinterface.media.ExifInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import android.telephony.SmsManager;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

// Dialog fragment for adding and editing an inventory item
// Operates in two modes: add mode (item is null) and edit mode (item is populated from args)
public class ItemDialogFragment extends DialogFragment {

    private EditText mItemNameEdit;
    private EditText mItemQuantityEdit;
    private ImageView mItemImage;
    private Button mIncrementButton;
    private Button mDecrementButton;
    private Button mDeleteButton;
    private EditText mItemSkuEdit;
    private EditText mItemPriceEdit;
    private EditText mItemCategoryEdit;
    private EditText mItemDetailsEdit;

    private InventoryItem mItem;  // Null for add mode, set for edit mode
    private OnDialogResultListener mListener;
    private String mCurrentImagePath;
    private Uri mCameraImageUri;

    // Launchers for camera, gallery, and camera permission request
    private ActivityResultLauncher<Uri> mCameraLauncher;
    private ActivityResultLauncher<String> mGalleryLauncher;
    private ActivityResultLauncher<String> mPermissionLauncher;

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

        // Register camera launcher
        mCameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success && mCameraImageUri != null) {
                        saveImageFromUri(mCameraImageUri);
                    }
                }
        );

        // Register gallery launcher
        mGalleryLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        saveImageFromUri(uri);
                    }
                }
        );

        // Register permission launcher, show camera dialog if permission granted
        mPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        showImageSourceDialog();
                    }
                }
        );
    }


    @SuppressLint("ClickableViewAccessibility")
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState){
        View view = getLayoutInflater().inflate(R.layout.dialog_item, null);

        mItemNameEdit = view.findViewById(R.id.item_text_name);
        mItemQuantityEdit = view.findViewById(R.id.item_quantity);
        mItemImage = view.findViewById(R.id.item_image);
        mIncrementButton = view.findViewById(R.id.increment_button);
        mDecrementButton = view.findViewById(R.id.decrement_button);
        mDeleteButton = view.findViewById(R.id.delete_button);
        mItemSkuEdit = view.findViewById(R.id.item_sku);
        mItemPriceEdit = view.findViewById(R.id.item_price);
        mItemCategoryEdit = view.findViewById(R.id.item_category);
        mItemDetailsEdit = view.findViewById(R.id.item_details);
        Button confirmButton = view.findViewById(R.id.confirm_button);
        Button exitButton = view.findViewById(R.id.exit_button);

        //Load existing data if in edit mode
        if (getArguments() != null && getArguments().containsKey("item")) {
            mItem = (InventoryItem) getArguments().getSerializable("item");

            mItemNameEdit.setText(mItem.getName());
            mItemQuantityEdit.setText(String.valueOf(mItem.getQuantity()));
            mItemSkuEdit.setText(mItem.getSku());
            mItemPriceEdit.setText(mItem.getPrice() != null ? String.valueOf(mItem.getPrice()) : "");
            mItemCategoryEdit.setText(mItem.getCategory());
            mItemDetailsEdit.setText(mItem.getDescription());

            if (mItem.getImageUrl() != null && !mItem.getImageUrl().isEmpty()) {
                mCurrentImagePath = mItem.getImageUrl();
                loadImage(mCurrentImagePath);
            }

            mDeleteButton.setVisibility(View.VISIBLE);
        } else {
            mItem = new InventoryItem();
            mDeleteButton.setVisibility(View.GONE);
        }

        // Increment button
        mIncrementButton.setOnClickListener(v -> {
            long currentQty = getQuantity();
            mItemQuantityEdit.setText(String.valueOf(currentQty + 1));
        });

        // Decrement button - floor of 0 to prevent negative quantities
        mDecrementButton.setOnClickListener(v -> {
            long currentQty = getQuantity();
            if(currentQty > 0) {
                mItemQuantityEdit.setText(String.valueOf(currentQty - 1));
            }
        });

        // Request camera permission if not granted, otherwise show image source dialog
        mItemImage.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                showImageSourceDialog();
            } else {
                mPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });

        // Confirm button
        confirmButton.setOnClickListener(v -> {
            saveItem();
        });

        // Show confirmation dialog before deleting
        mDeleteButton.setOnClickListener(v -> {
            if(mItem != null && mListener != null){
                new AlertDialog.Builder(requireContext())
                        .setTitle("Delete Item")
                        .setMessage("Are you sure you want to delete this item?")
                        .setPositiveButton("Delete", (dialog, which) -> {
                            mListener.onItemDeleted(mItem, success -> {
                                if(success) {
                                    dismiss();
                                } else {
                                    Toast.makeText(requireContext(), "Failed to delete item, please try again.", Toast.LENGTH_SHORT).show();
                                }
                            });
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });

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

        exitButton.setOnClickListener(v -> dismiss());

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        builder.setView(view);

        return builder.create();

    }

    // Prompts user to choose image gallery or camera as image source
    private void showImageSourceDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Add Photo")
                .setItems(new String[]{"Take Photo", "Choose from Gallery"}, (dialog, which) -> {
                    if (which == 0) {
                        launchCamera();
                    } else {
                        launchGallery();
                    }
                })
                .show();
    }

    // Creates a file for photo and launches camera
    private void launchCamera() {
        File photoFile = createImageFile();
        mCameraImageUri = FileProvider.getUriForFile(
                requireContext(),
                requireContext().getPackageName() + ".fileprovider",
                photoFile
        );
        mCameraLauncher.launch(mCameraImageUri);
    }

    private void launchGallery() {
        mGalleryLauncher.launch("image/*");
    }

    // Creates a unique file name in internal storage for saving captured image
    private File createImageFile() {
        String fileName = "item_" + System.currentTimeMillis() + ".jpg";
        File storageDir = requireContext().getFilesDir();
        return new File(storageDir, fileName);
    }

    // Copies image from URI into internal storage and updates displayed image
    private void saveImageFromUri(Uri uri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (inputStream != null) {
                inputStream.close();
            }

            InputStream exifStream = requireContext().getContentResolver().openInputStream(uri);

            if(exifStream != null) {
                ExifInterface exif = new ExifInterface(exifStream);
                exifStream.close();
                bitmap = rotateImageIfRequired(bitmap, exif);
            }


            // Save to internal storage
            String fileName = "item_" + System.currentTimeMillis() + ".jpg";
            File file = new File(requireContext().getFilesDir(), fileName);

            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.close();

            mCurrentImagePath = file.getAbsolutePath();
            loadImage(mCurrentImagePath);

        } catch (IOException e) {
            Log.e("ItemDialogFragment", "Failed to save image: " + e.getMessage());
        }
    }

    private void loadImage(String imagePath) {
        File imgFile = new File(imagePath);
        if (imgFile.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            mItemImage.setImageBitmap(bitmap);
            mItemImage.setImageTintList(null); //Removes green tint used for placeholder
        }
    }

    // Read EXIF orientation data and rotate bitmap to correct orientation if needed
    private Bitmap rotateImageIfRequired(Bitmap bitmap, ExifInterface exif) {
        int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                return rotateBitmap(bitmap, 90);
            case ExifInterface.ORIENTATION_ROTATE_180:
                return rotateBitmap(bitmap, 180);
            case ExifInterface.ORIENTATION_ROTATE_270:
                return rotateBitmap(bitmap, 270);
            default:
                return bitmap;
        }
    }

    private Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
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

    private void saveItem() {
        String name = mItemNameEdit.getText().toString().trim();
        long quantity;

        if(name.isEmpty()){
            mItemNameEdit.setError("Name Required");
            return;
        }

        String quantityText = mItemQuantityEdit.getText().toString().trim();
        if (quantityText.isEmpty()) {
            mItemQuantityEdit.setError("Quantity Required");
            return;
        }

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

        mItem.setName(name);
        mItem.setQuantity(quantity);
        mItem.setImageUrl(mCurrentImagePath);

        String sku = mItemSkuEdit.getText().toString().trim();
        mItem.setSku(sku.isEmpty() ? null : sku);

        String category = mItemCategoryEdit.getText().toString().trim();
        mItem.setCategory(category.isEmpty() ? null : category);

        String description = mItemDetailsEdit.getText().toString().trim();
        mItem.setDescription(description.isEmpty() ? null : description);

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
