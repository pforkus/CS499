package com.zybooks.inventorytracking;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import java.io.File;

public class ImagePickerController {

    public interface OnImageReadyListener {
        void onImagePreviewReady(Uri localUri);
    }

    private final Fragment mFragment;
    private final OnImageReadyListener mListener;

    private ActivityResultLauncher<Uri> mCameraLauncher;
    private ActivityResultLauncher<String> mGalleryLauncher;
    private ActivityResultLauncher<String> mPermissionLauncher;
    private Uri mCameraImageUri;


    public ImagePickerController(Fragment fragment, OnImageReadyListener listener) {
        mFragment = fragment;
        mListener = listener;
        registerLaunchers();
    }

    private void registerLaunchers() {
        mCameraLauncher = mFragment.registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success && mCameraImageUri != null) {
                        handleImageUri(mCameraImageUri);
                    }
                }
        );

        mGalleryLauncher = mFragment.registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) handleImageUri(uri);
                }
        );

        mPermissionLauncher = mFragment.registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) showImageSourceDialog();
                }
        );
    }

    // Entry point the fragment calls when the image view is tapped
    public void start() {
        Context context = mFragment.requireContext();
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            showImageSourceDialog();
        } else {
            mPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void showImageSourceDialog() {
        new AlertDialog.Builder(mFragment.requireContext())
                .setTitle("Add Photo")
                .setItems(new String[]{"Take Photo", "Choose from Gallery"}, (dialog, which) -> {
                    if (which == 0) launchCamera();
                    else mGalleryLauncher.launch("image/*");
                })
                .show();
    }

    private void launchCamera() {
        Context context = mFragment.requireContext();
        File photoFile = ImageStorageHelper.createImageFile(context.getFilesDir());
        mCameraImageUri = FileProvider.getUriForFile(
                context, context.getPackageName() + ".fileprovider", photoFile);
        mCameraLauncher.launch(mCameraImageUri);
    }

    private void handleImageUri(Uri uri) {
        mListener.onImagePreviewReady(uri);
        Log.d("ImageFlow", "handleImageUri called with: " + uri);
    }
}