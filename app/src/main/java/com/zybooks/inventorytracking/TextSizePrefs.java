package com.zybooks.inventorytracking;

import android.content.Context;

public class TextSizePrefs {
    private static final String PREFS_NAME = "settings";
    private static final String KEY_SIZE = "text_size";

    public static final String SMALL = "small";
    public static final String MEDIUM = "medium";
    public static final String LARGE = "large";

    private static final float SCALE_SMALL = 1f;
    private static final float SCALE_MEDIUM = 1.25f;
    private static final float SCALE_LARGE = 1.35f;

    public static void save(Context context, String size) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_SIZE, size)
                .apply();
    }

    public static String get(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_SIZE, SMALL); // Sets default to small
    }

    public static float getScaleFactor(Context context) {
        String size = get(context);
        switch(size) {
            case MEDIUM: return SCALE_MEDIUM;
            case LARGE: return SCALE_LARGE;
            default: return SCALE_SMALL;
        }

    }
}
