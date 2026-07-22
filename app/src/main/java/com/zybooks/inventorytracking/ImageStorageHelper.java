package com.zybooks.inventorytracking;
import android.net.Uri;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;

import java.io.File;
import java.util.Map;

public class ImageStorageHelper {



    public static File createImageFile(File storageDir) {
        String fileName = "item_" + System.currentTimeMillis() + ".jpg";
        return new File(storageDir, fileName);
    }

    public interface UploadCallback {
        void onStart();
        void onSuccess(String secureUrl, String publicId);
        void onError(String message);
    }

    public static void loadImage(Object imageSource, ImageView target) {
        target.setImageTintList(null);
        Glide.with(target.getContext())
                .load(imageSource)
                .placeholder(R.drawable.add_photo)
                .error(R.drawable.error_image)
                .into(target);
    }

    public static void uploadImage(Uri uri, UploadCallback callback) {
        MediaManager.get().upload(uri)
                .unsigned("image_preset")
                .callback(new com.cloudinary.android.callback.UploadCallback() {
                    @Override
                    public void onStart(String requestId) {
                        callback.onStart();
                    }

                    @Override
                    public void onProgress(String requestId, long bytes, long totalBytes) { }

                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String url = (String) resultData.get("secure_url");
                        String publicId = (String) resultData.get("public_id");
                        callback.onSuccess(url, publicId);
                    }

                    @Override
                    public void onError(String requestId, ErrorInfo error) {
                        callback.onError(error.getDescription());
                    }

                    @Override
                    public void onReschedule(String requestId, ErrorInfo error) { }
                })
                .dispatch();
    }
}
