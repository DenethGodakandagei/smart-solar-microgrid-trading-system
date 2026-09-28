/*
 * Smart Solar Microgrid Trading System
 * RegisterActivity.java
 *
 * Member 2 - Native Android Prosumer Application
 * Handles new prosumer registration with Sri Lankan NIC as the primary key.
 */
package com.smartsolar.app.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.smartsolar.app.R;
import com.smartsolar.app.api.ApiClient;
import com.smartsolar.app.api.ApiService;
import com.smartsolar.app.api.models.ApiError;
import com.smartsolar.app.api.models.ProsumerProfile;
import com.smartsolar.app.api.models.RegisterRequest;
import com.smartsolar.app.utils.NetworkUtils;
import com.smartsolar.app.utils.ValidationUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Prosumer registration activity.
 * Validates Sri Lankan NIC (9 digits + V/X or 12 digits), full name, email,
 * phone number, and password matching before submitting to C# Web API.
 */
public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilNic;
    private TextInputLayout tilFullName;
    private TextInputLayout tilEmail;
    private TextInputLayout tilPhone;
    private TextInputLayout tilAddress;
    private TextInputLayout tilPassword;
    private TextInputLayout tilConfirmPassword;

    private TextInputEditText etNic;
    private TextInputEditText etFullName;
    private TextInputEditText etEmail;
    private TextInputEditText etPhone;
    private TextInputEditText etAddress;
    private TextInputEditText etPassword;
    private TextInputEditText etConfirmPassword;

    private MaterialButton btnRegister;
    private ProgressBar progressBar;
    private TextView tvLoginLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        initViews();
        setupListeners();
    }

    /**
     * Initializes UI element references.
     */
    private void initViews() {
        tilNic = findViewById(R.id.tilNic);
        tilFullName = findViewById(R.id.tilFullName);
        tilEmail = findViewById(R.id.tilEmail);
        tilPhone = findViewById(R.id.tilPhone);
        tilAddress = findViewById(R.id.tilAddress);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);

        etNic = findViewById(R.id.etNic);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);

        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);
        tvLoginLink = findViewById(R.id.tvLoginLink);
    }

    /**
     * Sets up event listeners for inputs and actions.
     */
    private void setupListeners() {
        btnRegister.setOnClickListener(v -> attemptRegistration());
        tvLoginLink.setOnClickListener(v -> finish());
    }

    /**
     * Validates form inputs and sends registration API request.
     */
    private void attemptRegistration() {
        clearErrors();

        String nic = ValidationUtils.safeTrim(getText(etNic));
        String fullName = ValidationUtils.safeTrim(getText(etFullName));
        String email = ValidationUtils.safeTrim(getText(etEmail));
        String phone = ValidationUtils.safeTrim(getText(etPhone));
        String address = ValidationUtils.safeTrim(getText(etAddress));
        String password = getText(etPassword);
        String confirmPassword = getText(etConfirmPassword);

        boolean cancel = false;
        View focusView = null;

        // Confirm Password validation
        if (!ValidationUtils.doPasswordsMatch(password, confirmPassword)) {
            tilConfirmPassword.setError(getString(R.string.auth_error_password_mismatch));
            focusView = etConfirmPassword;
            cancel = true;
        }

        // Password validation
        if (!ValidationUtils.isValidPassword(password)) {
            tilPassword.setError(getString(R.string.auth_error_password_short));
            focusView = etPassword;
            cancel = true;
        }

        // Phone validation
        if (!ValidationUtils.isValidPhone(phone)) {
            tilPhone.setError(getString(R.string.auth_error_phone_required));
            focusView = etPhone;
            cancel = true;
        }

        // Email validation
        if (!ValidationUtils.isValidEmail(email)) {
            tilEmail.setError(getString(R.string.auth_error_email_invalid));
            focusView = etEmail;
            cancel = true;
        }

        // Full Name validation
        if (!ValidationUtils.isNotEmpty(fullName) || fullName.length() < 2) {
            tilFullName.setError(getString(R.string.auth_error_name_required));
            focusView = etFullName;
            cancel = true;
        }

        // NIC validation (Primary Key)
        if (!ValidationUtils.isValidNic(nic)) {
            tilNic.setError(getString(R.string.auth_error_nic_invalid));
            focusView = etNic;
            cancel = true;
        }

        if (cancel) {
            if (focusView != null) {
                focusView.requestFocus();
            }
            return;
        }

        // Check network connectivity
        if (!NetworkUtils.isNetworkAvailable(this)) {
            showSnackbar(getString(R.string.error_no_internet));
            return;
        }

        executeRegistration(nic, fullName, email, phone, address, password);
    }

    /**
     * Executes the registration API call.
     */
    private void executeRegistration(String nic, String fullName, String email,
                                    String phone, String address, String password) {
        setLoading(true);

        RegisterRequest request = new RegisterRequest(nic, fullName, email, phone, address, password);
        ApiService apiService = ApiClient.getApiService(this);

        apiService.register(request).enqueue(new Callback<ProsumerProfile>() {
            @Override
            public void onResponse(@NonNull Call<ProsumerProfile> call,
                                   @NonNull Response<ProsumerProfile> response) {
                setLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    showSuccessDialog(nic);
                } else {
                    String errorMessage = parseErrorMessage(response);
                    showSnackbar(errorMessage);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProsumerProfile> call, @NonNull Throwable t) {
                setLoading(false);
                showSnackbar(getString(R.string.auth_error_register_failed) + ": " + t.getMessage());
            }
        });
    }

    /**
     * Displays a success dialog after registration and routes to LoginActivity.
     *
     * @param registeredNic Registered NIC to prefill in login form.
     */
    private void showSuccessDialog(String registeredNic) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.label_success)
                .setMessage(R.string.auth_success_register)
                .setPositiveButton(R.string.auth_btn_login, (dialog, which) -> {
                    Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                    intent.putExtra(LoginActivity.EXTRA_REGISTERED_NIC, registeredNic);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    finish();
                })
                .setCancelable(false)
                .show();
    }

    /**
     * Parses error response body if available.
     */
    private String parseErrorMessage(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String errorJson = response.errorBody().string();
                ApiError apiError = new Gson().fromJson(errorJson, ApiError.class);
                if (apiError != null && apiError.getMessage() != null) {
                    return apiError.getMessage();
                }
            }
        } catch (Exception ignored) {
        }
        return getString(R.string.auth_error_register_failed);
    }

    /**
     * Clears all validation error messages on form fields.
     */
    private void clearErrors() {
        tilNic.setError(null);
        tilFullName.setError(null);
        tilEmail.setError(null);
        tilPhone.setError(null);
        tilAddress.setError(null);
        tilPassword.setError(null);
        tilConfirmPassword.setError(null);
    }

    /**
     * Safely reads text from an EditText.
     */
    private String getText(TextInputEditText editText) {
        return editText != null && editText.getText() != null ? editText.getText().toString() : "";
    }

    /**
     * Shows or hides the loading progress state.
     *
     * @param isLoading true if operation in progress.
     */
    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!isLoading);
        etNic.setEnabled(!isLoading);
        etFullName.setEnabled(!isLoading);
        etEmail.setEnabled(!isLoading);
        etPhone.setEnabled(!isLoading);
        etAddress.setEnabled(!isLoading);
        etPassword.setEnabled(!isLoading);
        etConfirmPassword.setEnabled(!isLoading);
        tvLoginLink.setEnabled(!isLoading);
    }

    /**
     * Helper to display a Snackbar message.
     */
    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }
}
