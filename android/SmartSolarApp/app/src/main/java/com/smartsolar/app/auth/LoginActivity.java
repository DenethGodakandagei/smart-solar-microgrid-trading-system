package com.smartsolar.app.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.smartsolar.app.api.models.LoginRequest;
import com.smartsolar.app.api.models.LoginResponse;
import com.smartsolar.app.dashboard.DashboardActivity;
import com.smartsolar.app.operator.OperatorHomeActivity;
import com.smartsolar.app.utils.Constants;
import com.smartsolar.app.utils.NetworkUtils;
import com.smartsolar.app.utils.ValidationUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Handles prosumer and operator login strictly using NIC and Password.
 * - Auto-routes if already authenticated.
 * - Validates NIC and password inputs.
 * - Sends authentication request to C# Web API.
 * - Stores JWT token in SharedPreferences & SQLite.
 * - Routes user to role-specific home (Dashboard vs. OperatorHome).
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LOGIN";

    public static final String EXTRA_REGISTERED_NIC = "extra_registered_nic";

    private TextInputLayout tilNic;
    private TextInputLayout tilPassword;
    private TextInputEditText etNic;
    private TextInputEditText etPassword;
    private MaterialButton btnLogin;
    private ProgressBar progressBar;
    private TextView tvRegisterLink;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sessionManager = SessionManager.getInstance(this);

        // Check if user is already logged in
        if (sessionManager.isLoggedIn()) {
            routeByRole(sessionManager.getUserRole());
            finish();
            return;
        }

        setContentView(R.layout.activity_login);

        initViews();
        setupListeners();
        checkPrefill();
    }

    /**
     * Initializes UI element references.
     */
    private void initViews() {
        tilNic = findViewById(R.id.tilNic);
        tilPassword = findViewById(R.id.tilPassword);
        etNic = findViewById(R.id.etNic);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);
        tvRegisterLink = findViewById(R.id.tvRegisterLink);
    }

    /**
     * Sets up event listeners for buttons and links.
     */
    private void setupListeners() {
        btnLogin.setOnClickListener(v -> attemptLogin());

        tvRegisterLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    /**
     * Prefills NIC field if returned from successful registration.
     */
    private void checkPrefill() {
        String registeredNic = getIntent().getStringExtra(EXTRA_REGISTERED_NIC);
        if (registeredNic != null && !registeredNic.isEmpty()) {
            etNic.setText(registeredNic);
            etPassword.requestFocus();
        }
    }

    /**
     * Validates inputs and initiates authentication API request using NIC.
     */
    private void attemptLogin() {
        tilNic.setError(null);
        tilPassword.setError(null);

        String nic = ValidationUtils.safeTrim(
                etNic.getText() != null ? etNic.getText().toString() : "");
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";

        // Validate NIC
        if (TextUtils.isEmpty(nic)) {
            tilNic.setError(getString(R.string.auth_error_nic_required));
            etNic.requestFocus();
            return;
        }

        // Validate password
        if (TextUtils.isEmpty(password)) {
            tilPassword.setError(getString(R.string.auth_error_password_required));
            etPassword.requestFocus();
            return;
        }

        // Check network connection
        if (!NetworkUtils.isNetworkAvailable(this)) {
            showSnackbar(getString(R.string.error_no_internet));
            return;
        }

        executeLogin(nic, password);
    }

    /**
     * Sends the login API call via Retrofit.
     *
     * @param nic      User's NIC.
     * @param password User password.
     */
    private void executeLogin(final String nic, final String password) {
        setLoading(true);

        LoginRequest request = new LoginRequest();
        request.setNic(nic);
        request.setPassword(password);

        ApiService apiService = ApiClient.getApiService(this);
        apiService.login(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponse> call,
                    @NonNull Response<LoginResponse> response) {
                setLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    final LoginResponse loginResponse = response.body();

                    // Ensure NIC is set in the session object
                    if (loginResponse.getNic() == null || loginResponse.getNic().isEmpty()) {
                        loginResponse.setNic(nic);
                    }

                    // Save session to SharedPreferences
                    try {
                        sessionManager.saveLoginSession(loginResponse);
                    } catch (Exception e) {
                        Log.e(TAG, "Saving session to SharedPreferences failed", e);
                        showSnackbar("Could not save session: " + e.getMessage());
                        return;
                    }

                    // Persist session to local SQLite OFF the main thread.
                    new Thread(() -> {
                        try {
                            SmartSolarApplication app = SmartSolarApplication.getInstance();
                            if (app != null && app.getUserDao() != null) {
                                app.getUserDao().saveSession(loginResponse);
                            }
                        } catch (Exception e) {
                            Log.e(TAG, "SQLite session save failed", e);
                        }
                    }).start();

                    Toast.makeText(LoginActivity.this,
                            "Welcome back, " + (loginResponse.getFullName() != null
                                    ? loginResponse.getFullName()
                                    : loginResponse.getNic()),
                            Toast.LENGTH_SHORT).show();

                    // Route based on role
                    routeByRole(loginResponse.getRole());
                    finish();
                } else {
                    String errorMessage = parseErrorMessage(response);
                    showSnackbar(errorMessage);
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponse> call, @NonNull Throwable t) {
                setLoading(false);
                Log.e(TAG, "Login request failed", t);
                showSnackbar(getString(R.string.auth_error_login_failed) + ": " + t.getMessage());
            }
        });
    }

    /**
     * Parses error response body if available, and logs the real HTTP error.
     */
    private String parseErrorMessage(Response<?> response) {
        try {
            if (response.errorBody() != null) {
                String errorJson = response.errorBody().string();
                Log.e(TAG, "HTTP " + response.code() + ": " + errorJson);
                ApiError apiError = new Gson().fromJson(errorJson, ApiError.class);
                if (apiError != null && apiError.getDisplayMessage() != null) {
                    return apiError.getDisplayMessage();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Could not parse error body", e);
        }

        return getString(R.string.auth_error_login_failed) + " (HTTP " + response.code() + ")";
    }

    /**
     * Routes the user to the appropriate screen according to role.
     * Prosumer -> DashboardActivity
     * Operator / GridOperator -> OperatorHomeActivity
     *
     * @param role User role string.
     */
    private void routeByRole(String role) {
        Intent intent;
        if (Constants.ROLE_OPERATOR.equalsIgnoreCase(role) || Constants.ROLE_GRID_OPERATOR.equalsIgnoreCase(role)) {
            intent = new Intent(this, OperatorHomeActivity.class);
        } else {
            intent = new Intent(this, DashboardActivity.class);
        }
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    /**
     * Shows or hides the loading progress state.
     *
     * @param isLoading true if operation in progress.
     */
    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!isLoading);
        etNic.setEnabled(!isLoading);
        etPassword.setEnabled(!isLoading);
        tvRegisterLink.setEnabled(!isLoading);
    }

    /**
     * Helper to display a Snackbar message.
     */
    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }
}