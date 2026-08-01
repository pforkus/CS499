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
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.lifecycle.ViewModelProvider;

import android.graphics.Rect;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;

// Handles user login and account creation
public class LoginActivity extends BaseActivity {

    private EditText mUsernameEdit;
    private EditText mPasswordEdit;
    private TextView mErrorText;
    private UserViewModel mViewModel;
    private Button mLoginButton;
    private Button mCreateButton;
    private ProgressBar mProgressBar;

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
        mLoginButton = findViewById(R.id.login);
        mCreateButton = findViewById(R.id.create);
        mErrorText = findViewById(R.id.login_error);
        mProgressBar = findViewById(R.id.progressBar);

        mViewModel = new ViewModelProvider(this)
                .get(UserViewModel.class);

        setupCreateListener();
        setupLoginListener();

        // Disable buttons until server connection is established
        mLoginButton.setEnabled(false);
        mCreateButton.setEnabled(false);

        mViewModel.getServerState().observe(this, state -> {
            switch(state) {
                case WAKING:
                    mProgressBar.setVisibility(View.VISIBLE);
                    showStatus("Spinning up greatness");
                    break;
                case READY:
                    mProgressBar.setVisibility(View.GONE);
                    hideStatus();
                    mLoginButton.setEnabled(true);
                    mCreateButton.setEnabled(true);
                    break;
                case FAILED:
                    mProgressBar.setVisibility(View.GONE);
                    mLoginButton.setEnabled(true);
                    mCreateButton.setEnabled(true);
                    showStatus("Could not reach the server, please try again");
                    break;
            }
        });

        setupForgotPasswordListener();
        observeUser();

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
    private void showStatus(String message) {
        mErrorText.setText(message);
        mErrorText.setVisibility(View.VISIBLE);
    }

    private void hideStatus() {
        mErrorText.setVisibility(View.GONE);
    }
    private void setupLoginListener() {
        Button loginButton = findViewById(R.id.login);
        loginButton.setOnClickListener(v -> doLogin());
    }
    private void setupCreateListener(){
        Button createButton = findViewById(R.id.create);
        createButton.setOnClickListener(v ->  doCreate());
    }

    private void doCreate() {
        String username = mUsernameEdit.getText().toString().trim();
        String password = mPasswordEdit.getText().toString().trim();

        // Check if fields were left empty
        if(username.isEmpty() || password.isEmpty()) {
            showStatus("Please enter username and password");
            return;
        }

        mViewModel.createUser(username, password);
    }

    private void doLogin() {
        String username = mUsernameEdit.getText().toString().trim();
        String password = mPasswordEdit.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showStatus("Please enter username and password");
            return;
        }

        mViewModel.login(username, password);
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

    private void observeUser() {
        mViewModel.getUser().observe(this, user -> {
            if(user != null) {
                proceedAfterLogin();
            }
        });

        mViewModel.getError().observe(this, error -> {
            if(error != null) {
                showStatus(error);
            }
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