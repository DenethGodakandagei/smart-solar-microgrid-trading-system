/*
 * Smart Solar Microgrid Trading System
 * DeactivateAccountActivity.java
 *
 * Member 2 - Native Android Prosumer Application
 * Handles requesting prosumer account deactivation and subsequent local logout.
 */
package com.smartsolar.app.account;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.smartsolar.app.R;
import com.smartsolar.app.SmartSolarApplication;
import com.smartsolar.app.api.ApiClient;
import com.smartsolar.app.api.ApiService;
import com.smartsolar.app.api.models.ApiError;
import com.smartsolar.app.api.models.ProsumerProfile;
import com.smartsolar.app.auth.LoginActivity;
import com.smartsolar.app.auth.SessionManager;
import com.smartsolar.app.utils.NetworkUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Account deactivation confirmation activity.
 * Sends POST /api/prosumer/{nic}/deactivate to set account status to Deactivated.
 * Upon success, wipes local session and navigates to LoginActivity.
 */
public class DeactivateAccountActivity extends AppCompatActivity {

    private TextInputLayout tilReason;
    private TextInputEditText etReason;
    private MaterialButton btnConfirmDeactivate;
    private MaterialButton btnCancel;
    private ProgressBar progressBar;

    private SessionManager sessionManager;
    private String userNic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deactivate);

        sessionManager = SessionManager.getInstance(this);

        if (!sessionManager.isLoggedIn()) {
            finish();
            return;
        }

        userNic = sessionManager.getUserNic();

        initViews();
        setupListeners();
    }

    /**
     * Initializes UI element references.
     */
    private void initViews() {
        tilReason = findViewById(R.id.tilReason);
        etReason = findViewById(R.id.etReason);
        btnConfirmDeactivate = findViewById(R.id.btnConfirmDeactivate);
        btnCancel = findViewById(R.id.btnCancel);
        progressBar = findViewById(R.id.progressBar);
    }

    /**
     * Sets up event listeners.
     */
    private void setupListeners() {
        btnConfirmDeactivate.setOnClickListener(v -> showFinalConfirmationDialog());
        btnCancel.setOnClickListener(v -> finish());
    }

    /**
     * Prompts the user with a final modal confirmation before proceeding.
     */
    private void showFinalConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.deactivate_title)
                .setMessage(R.string.deactivate_warning)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(R.string.action_confirm, (dialog, which) -> executeDeactivation())
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    /**
     * Sends deactivation request to the API.
     */
    private void executeDeactivation() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            showSnackbar(getString(R.string.error_no_internet));
            return;
        }

        setLoading(true);

        ApiService apiService = ApiClient.getApiService(this);
        apiService.deactivateAccount(userNic).enqueue(new Callback<ProsumerProfile>() {
            @Override
            public void onResponse(@NonNull Call<ProsumerProfile> call,
                                   @NonNull Response<ProsumerProfile> response) {
                setLoading(false);

                if (response.isSuccessful()) {
                    showSuccessDialog();
                } else {
                    String errorMessage = parseErrorMessage(response);
                    showSnackbar(errorMessage);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProsumerProfile> call, @NonNull Throwable t) {
                setLoading(false);
                showSnackbar(getString(R.string.deactivate_failed) + ": " + t.getMessage());
            }
        });
    }

    /**
     * Displays a success dialog, clears all credentials, and routes to LoginActivity.
     */
    private void showSuccessDialog() {
        // Clear all local session data and caches
        SmartSolarApplication app = SmartSolarApplication.getInstance();
        if (app != null) {
            app.performLogout();
        } else {
            sessionManager.logout();
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.label_success)
                .setMessage(R.string.deactivate_success)
                .setPositiveButton(R.string.auth_btn_login, (dialog, which) -> {
                    Intent intent = new Intent(DeactivateAccountActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
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
        return getString(R.string.deactivate_failed);
    }

    /**
     * Sets loading state.
     */
    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnConfirmDeactivate.setEnabled(!isLoading);
        btnCancel.setEnabled(!isLoading);
        etReason.setEnabled(!isLoading);
    }

    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }
}
