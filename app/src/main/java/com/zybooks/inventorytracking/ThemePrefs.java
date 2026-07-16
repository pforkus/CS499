package com.zybooks.inventorytracking;

import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;

public class ThemePrefs {
    private static final String PREFS_NAME = "settings";
    private static final String KEY_THEME = "theme_mode";

    public static final String LIGHT = "light";
    public static final String DARK = "dark";
    public static final String SYSTEM = "system";

    // Sets, applies and saves selected theme
    public static void save(Context context, String mode) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_THEME, mode)
                .apply();
    }

    // Gets the currently set theme
    public static String get(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_THEME, SYSTEM);
    }

    // Applies the selected theme
    public static void applyTheme(String mode) {
        int nightMode;
        switch (mode) {
            case LIGHT:
                nightMode = AppCompatDelegate.MODE_NIGHT_NO;
                break;
            case DARK:
                nightMode = AppCompatDelegate.MODE_NIGHT_YES;
                break;
            default:
                nightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
                break;
        }
        AppCompatDelegate.setDefaultNightMode(nightMode);
    }
}
