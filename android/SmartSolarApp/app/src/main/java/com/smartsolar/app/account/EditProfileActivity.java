/*
 * Smart Solar Microgrid Trading System
 * EditProfileActivity.java
 *
 * Member 2 - Native Android Prosumer Application
 * Handles modifying prosumer contact details and residential address.
 * NIC is the immutable primary key and remains locked.
 */
package com.smartsolar.app.account;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
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
import com.smartsolar.app.api.models.ProsumerProfile;
import com.smartsolar.app.auth.SessionManager;
import com.smartsolar.app.db.UserDao;
import com.smartsolar.app.utils.NetworkUtils;
import com.smartsolar.app.utils.ValidationUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Allows prosumers to update their profile information.
 * Validates inputs using ValidationUtils and sends PUT /api/prosumer/{nic}.
 */
public class EditProfileActivity extends AppCompatActivity {

    private TextInputLayout tilNic;
    private TextInputLayout tilFullName;
    private TextInputLayout tilEmail;
    private TextInputLayout tilPhone;
    private TextInputLayout tilAddress;

    private TextInputEditText etNic;
    private TextInputEditText etFullName;
    private TextInputEditText etEmail;
    private TextInputEditText etPhone;
    private TextInputEditText etAddress;

    private MaterialButton btnSave;
    private MaterialButton btnCancel;
    private ProgressBar progressBar;

    private SessionManager sessionManager;
    private UserDao userDao;
    private String userNic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        sessionManager = SessionManager.getInstance(this);
        userDao = SmartSolarApplication.getInstance().getUserDao();

        initViews();
        setupListeners();
        populateInitialData();
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

        etNic = findViewById(R.id.etNic);
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        etAddress = findViewById(R.id.etAddress);

        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        progressBar = findViewById(R.id.progressBar);
    }

    /**
     * Sets up click listeners.
     */
    private void setupListeners() {
        btnSave.setOnClickListener(v -> attemptSave());
        btnCancel.setOnClickListener(v -> finish());
    }

    /**
     * Pre-populates the input fields with current profile data.
     */
    private void populateInitialData() {
        Intent intent = getIntent();
        userNic = intent.getStringExtra(ProfileActivity.EXTRA_PROFILE_NIC);
        if (userNic == null || userNic.isEmpty()) {
            userNic = sessionManager.getUserNic();
        }

        etNic.setText(userNic);

        // Pre-fill from Intent if available, else load from local database / session
        String name = intent.getStringExtra(ProfileActivity.EXTRA_PROFILE_NAME);
        String email = intent.getStringExtra(ProfileActivity.EXTRA_PROFILE_EMAIL);
        String phone = intent.getStringExtra(ProfileActivity.EXTRA_PROFILE_PHONE);
        String address = intent.getStringExtra(ProfileActivity.EXTRA_PROFILE_ADDRESS);

        if (name == null && userDao != null) {
            ProsumerProfile profile = userDao.getProsumerProfile(userNic);
            if (profile != null) {
                name = profile.getFullName();
                email = profile.getEmail();
                phone = profile.getPhone();
                address = profile.getAddress();
            }
        }

        etFullName.setText(name != null ? name : sessionManager.getUserName());
        etEmail.setText(email != null ? email : sessionManager.getUserEmail());
        etPhone.setText(phone != null ? phone : "");
        etAddress.setText(address != null ? address : "");
    }

    /**
     * Validates inputs and sends update request to API.
     */
    private void attemptSave() {
        clearErrors();

        String fullName = ValidationUtils.safeTrim(getText(etFullName));
        String email = ValidationUtils.safeTrim(getText(etEmail));
        String phone = ValidationUtils.safeTrim(getText(etPhone));
        String address = ValidationUtils.safeTrim(getText(etAddress));

        boolean cancel = false;
        View focusView = null;

        // Validate phone number
        if (!ValidationUtils.isValidPhone(phone)) {
            tilPhone.setError(getString(R.string.auth_error_phone_required));
            focusView = etPhone;
            cancel = true;
        }

        // Validate email
        if (!ValidationUtils.isValidEmail(email)) {
            tilEmail.setError(getString(R.string.auth_error_email_invalid));
            focusView = etEmail;
            cancel = true;
        }

        // Validate full name
        if (!ValidationUtils.isNotEmpty(fullName) || fullName.length() < 2) {
            tilFullName.setError(getString(R.string.auth_error_name_required));
            focusView = etFullName;
            cancel = true;
        }

        if (cancel) {
            if (focusView != null) {
                focusView.requestFocus();
            }
            return;
        }

        // Check network connection
        if (!NetworkUtils.isNetworkAvailable(this)) {
            showSnackbar(getString(R.string.error_no_internet));
            return;
        }

        executeUpdate(fullName, email, phone, address);
    }

    /**
     * Submits the updated profile to the API.
     */
    private void executeUpdate(String fullName, String email, String phone, String address) {
        setLoading(true);

        ProsumerProfile updated = new ProsumerProfile();
        updated.setNic(userNic);
        updated.setFullName(fullName);
        updated.setEmail(email);
        updated.setPhone(phone);
        updated.setAddress(address);

        ApiService apiService = ApiClient.getApiService(this);
        apiService.updateProfile(userNic, updated).enqueue(new Callback<ProsumerProfile>() {
            @Override
            public void onResponse(@NonNull Call<ProsumerProfile> call,
                                   @NonNull Response<ProsumerProfile> response) {
                setLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    ProsumerProfile result = response.body();

                    // Update local SQLite cache
                    if (userDao != null) {
                        userDao.saveSession(result, sessionManager.getAuthToken());
                    }

                    // Update SharedPreferences
                    sessionManager.updateUserProfile(result.getFullName(), result.getEmail());

                    Toast.makeText(EditProfileActivity.this,
                            R.string.profile_update_success, Toast.LENGTH_SHORT).show();

                    setResult(RESULT_OK);
                    finish();
                } else {
                    String errorMessage = parseErrorMessage(response);
                    showSnackbar(errorMessage);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProsumerProfile> call, @NonNull Throwable t) {
                setLoading(false);
                showSnackbar(getString(R.string.profile_update_failed) + ": " + t.getMessage());
            }
        });
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
        return getString(R.string.profile_update_failed);
    }

    /**
     * Clears error messages on all inputs.
     */
    private void clearErrors() {
        tilFullName.setError(null);
        tilEmail.setError(null);
        tilPhone.setError(null);
        tilAddress.setError(null);
    }

    /**
     * Reads text safely from EditText.
     */
    private String getText(TextInputEditText editText) {
        return editText != null && editText.getText() != null ? editText.getText().toString() : "";
    }

    /**
     * Sets loading state.
     */
    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!isLoading);
        btnCancel.setEnabled(!isLoading);
        etFullName.setEnabled(!isLoading);
        etEmail.setEnabled(!isLoading);
        etPhone.setEnabled(!isLoading);
        etAddress.setEnabled(!isLoading);
    }

    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }
}
