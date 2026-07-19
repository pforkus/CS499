package com.zybooks.inventorytracking;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import com.google.android.material.color.MaterialColors;
import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;


import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Handles user login and account creation
public class LoginActivity extends BaseActivity {

    private EditText mUsernameEdit;
    private EditText mPasswordEdit;
    private TextView mErrorText;
    private InventoryRepository mRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mUsernameEdit = findViewById(R.id.username);
        mPasswordEdit = findViewById(R.id.password);
        mErrorText = findViewById(R.id.login_error);
        mRepository = InventoryRepository.getInstance(getApplicationContext());


        setupClickListener();
        setupLoginListener();
        setupForgotPasswordListener();
        testServer();

    }

    // Dismiss keyboard when tapping outside an EditText
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)ev.getRawX(), (int)ev.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }
    private void showError(String message) {
        mErrorText.setText(message);
        mErrorText.setVisibility(View.VISIBLE);
    }

    // TODO just for testing local server connection
    private void testServer() {
        String search = null;
        String category = null;
        String sort = null;
        String order = null;
        Integer page = null;
        Integer limit = null;

        ApiService api = RetrofitClient.getInstance().create(ApiService.class);
        api.getItems(search, category, sort, order, page, limit).enqueue(new Callback<ItemsResponse>() {
            @Override
            public void onResponse(Call<ItemsResponse> call, Response<ItemsResponse> response) {
                Log.d("ITEMS_TEST", "Response code: " + response.code());
                if (response.isSuccessful() && response.body() != null) {
                    List<InventoryItem> items = response.body().getItems();
                    Log.d("ITEMS_TEST", "Item count: " + items.size());
                    for (InventoryItem item : items) {
                        Log.d("ITEMS_TEST", "Item: " + item.toString());
                    }
                } else {
                    Log.e("ITEMS_TEST", "Unsuccessful: " + response.code());
                    try {
                        Log.e("ITEMS_TEST", "Error body: " + response.errorBody().string());
                    } catch (Exception e) { }
                }
            }

            @Override
            public void onFailure(Call<ItemsResponse> call, Throwable t) {
                Log.e("ITEMS_TEST", "Failed: " + t.getMessage(), t);
            }
        });
    }
    private void setupLoginListener() {
        Button loginButton = findViewById(R.id.login);
        loginButton.setOnClickListener(v -> {
            String username = mUsernameEdit.getText().toString().trim();
            String password = mPasswordEdit.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                showError("Please enter username and password");
                return;
            }

            // Check credentials against database
            mRepository.getUser(username, password, user -> {
                if(user != null) {
                    runOnUiThread(() -> proceedAfterLogin());
                } else {
                    // Run UI updates on main thread as callback comes from background threads
                    runOnUiThread(() -> showError("Invalid username or password"));
                }
            });
        });
    }
    private void setupClickListener(){
        Button createButton = findViewById(R.id.create);
        createButton.setOnClickListener(v ->  {

            String username = mUsernameEdit.getText().toString().trim();
            String password = mPasswordEdit.getText().toString().trim();

            // Check if fields were left empty
            if(username.isEmpty() || password.isEmpty()) {
                showError("Please enter username and password");
                return;
            }

            // Check if username already exists
            mRepository.getUserByUsername(username, existingUser -> {
                if (existingUser != null) {
                    // Run UI updates on main thread since callback comes from background thread
                    runOnUiThread(() -> showError("Account already exists"));
                } else {
                    User newUser = new User(username, password);
                    mRepository.addUser(newUser, createdUser -> {
                        runOnUiThread(() -> proceedAfterLogin());
                    });
                }
            });
        });
    }
    private void setupForgotPasswordListener() {
        TextView forgotPassword = findViewById(R.id.forgot_password);
        // Show a dialog explaining that password recovery is unavailable
        forgotPassword.setOnClickListener(v -> {
            AlertDialog dialog = new AlertDialog.Builder(this)
                    .setTitle("Forgot Password")
                    .setMessage("Password recovery is not available. Please create a new account if you cannot remember your credentials.")
                    .setPositiveButton("OK", null)
                    .create();
            dialog.show();

            // Set button colors explicitly for contrast
            int color = MaterialColors.getColor(this, com.google.android.material.R.attr.colorSecondary, Color.BLACK);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(color);
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(color);
        });
    }

    // Navigates to SMS permission screen on first login,
    // after first request - goes straight to dashboard
    private void proceedAfterLogin() {
        SharedPreferences prefs = getSharedPreferences("InventoryPrefs", MODE_PRIVATE);
        boolean smsPromptShown = prefs.getBoolean("sms_prompt_shown", false);
        Log.d("SMS", "smsPromptShown: " + smsPromptShown);
        Intent intent;
        if(!smsPromptShown) {
            intent = new Intent(this, SmsPermissionActivity.class);
        } else {
            intent = new Intent(this, DashboardActivity.class);
        }
        startActivity(intent);
        finish();
    }
}