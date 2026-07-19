package com.zybooks.inventorytracking;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.util.Log;
import android.widget.ImageView;

import androidx.exifinterface.media.ExifInterface;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ImageStorageHelper {

    public static File createImageFile(File storageDir) {
        String fileName = "item_" + System.currentTimeMillis() + ".jpg";
        return new File(storageDir, fileName);
    }

    // Returns saved file path, or null on failure
    public static String saveImageFromUri(Uri uri, ContentResolver resolver, File storageDir) {
        try {
            InputStream inputStream = resolver.openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (inputStream != null) inputStream.close();

            InputStream exifStream = resolver.openInputStream(uri);
            if (exifStream != null) {
                ExifInterface exif = new ExifInterface(exifStream);
                exifStream.close();
                bitmap = rotateImageIfRequired(bitmap, exif);
            }

            File file = createImageFile(storageDir);
            try (FileOutputStream fos = new FileOutputStream(file)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            }
            return file.getAbsolutePath();

        } catch (IOException e) {
            Log.e("ImageStorageHelper", "Failed to save image: " + e.getMessage());
            return null;
        }
    }

    public static void loadImage(String imagePath, ImageView target) {
        File imgFile = new File(imagePath);
        if (imgFile.exists()) {
            Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
            target.setImageBitmap(bitmap);
            target.setImageTintList(null);
        }
    }

    private static Bitmap rotateImageIfRequired(Bitmap bitmap, ExifInterface exif) {
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

    private static Bitmap rotateBitmap(Bitmap bitmap, int degrees) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degrees);
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
    }
}
