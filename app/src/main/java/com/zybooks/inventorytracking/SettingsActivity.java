package com.zybooks.inventorytracking;

import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButtonToggleGroup;

import org.w3c.dom.Text;

import java.util.Arrays;
import java.util.Objects;

public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        MaterialToolbar toolbar = findViewById(R.id.settings_toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Settings");
        toolbar.setNavigationOnClickListener(v -> finish());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.settings_root), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
            );
            return insets;
        });

        // Settings
        setupThemeToggle();
        setupTextSizeToggle();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }

    private void setupThemeToggle() {
        MaterialButtonToggleGroup toggleGroup = findViewById(R.id.themeToggleGroup);
        String currentMode = ThemePrefs.get(this);
        int currentButtonId = getButtonIdForMode(currentMode);
        toggleGroup.check(currentButtonId);

        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (!isChecked) return;
            String selected = getModeForButtonId(checkedId);
            ThemePrefs.save(this, selected);
            ThemePrefs.applyTheme(selected);
        });
    }

    private int getButtonIdForMode(String mode) {
        switch (mode) {
            case ThemePrefs.LIGHT: return R.id.btnLight;
            case ThemePrefs.DARK: return R.id.btnDark;
            default: return R.id.btnSystem;
        }
    }

    private String getModeForButtonId(int buttonId) {
        if (buttonId == R.id.btnLight) return ThemePrefs.LIGHT;
        if (buttonId == R.id.btnDark) return ThemePrefs.DARK;
        return ThemePrefs.SYSTEM;
    }

    private void setupTextSizeToggle() {
        MaterialButtonToggleGroup textToggleGroup = findViewById(R.id.textSizeToggleGroup);

        String currentSize = TextSizePrefs.get(this);
        int currentButtonId = getButtonIdForSize(currentSize);
        textToggleGroup.check(currentButtonId);

        textToggleGroup.addOnButtonCheckedListener((MaterialButtonToggleGroup group, int checkedId, boolean isChecked) -> {
            if(!isChecked) return;
            String selected = getSizeForButtonId(checkedId);
            TextSizePrefs.save(this, selected);
            recreate();
        });
    }

    private int getButtonIdForSize(String size) {
        switch (size) {
            case TextSizePrefs.MEDIUM: return R.id.btnMedium;
            case TextSizePrefs.LARGE: return R.id.btnLarge;
            default: return R.id.btnSmall;
        }
    }

    private String getSizeForButtonId(int buttonId) {
        if(buttonId == R.id.btnMedium) return TextSizePrefs.MEDIUM;
        if(buttonId == R.id.btnLarge) return TextSizePrefs.LARGE;
        return TextSizePrefs.SMALL;
    }

}
