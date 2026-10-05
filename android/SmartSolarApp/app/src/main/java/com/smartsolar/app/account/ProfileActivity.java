/*
 * Smart Solar Microgrid Trading System
 * ProfileActivity.java
 *
 * Member 2 - Native Android Prosumer Application
 * Displays prosumer profile details, account status, and options to edit, deactivate, or logout.
 */
package com.smartsolar.app.account;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.smartsolar.app.R;
import com.smartsolar.app.SmartSolarApplication;
import com.smartsolar.app.api.ApiClient;
import com.smartsolar.app.api.ApiService;
import com.smartsolar.app.api.models.ProsumerProfile;
import com.smartsolar.app.auth.LoginActivity;
import com.smartsolar.app.auth.SessionManager;
import com.smartsolar.app.db.UserDao;
import com.smartsolar.app.utils.DateTimeUtils;
import com.smartsolar.app.utils.NetworkUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Prosumer Profile Activity.
 * Displays profile attributes from local SQLite database first (offline-first),
 * then synchronizes with the C# Web API.
 */
public class ProfileActivity extends AppCompatActivity {

    public static final String EXTRA_PROFILE_NIC = "extra_profile_nic";
    public static final String EXTRA_PROFILE_NAME = "extra_profile_name";
    public static final String EXTRA_PROFILE_EMAIL = "extra_profile_email";
    public static final String EXTRA_PROFILE_PHONE = "extra_profile_phone";
    public static final String EXTRA_PROFILE_ADDRESS = "extra_profile_address";

    private SwipeRefreshLayout swipeRefreshLayout;
    private TextView tvAvatarInitials;
    private TextView tvProfileName;
    private TextView tvProfileNic;
    private TextView tvProfileStatus;
    private TextView tvProfileEmail;
    private TextView tvProfilePhone;
    private TextView tvProfileAddress;
    private TextView tvProfileMemberSince;

    private MaterialButton btnEditProfile;
    private MaterialButton btnDeactivateAccount;
    private MaterialButton btnLogout;
    private ProgressBar progressBar;

    private SessionManager sessionManager;
    private UserDao userDao;
    private String userNic;
    private ProsumerProfile currentProfile;

    private ActivityResultLauncher<Intent> editProfileLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        sessionManager = SessionManager.getInstance(this);

        // Verify active login session
        if (!sessionManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        userNic = sessionManager.getUserNic();
        userDao = SmartSolarApplication.getInstance().getUserDao();

        initViews();
        setupLauncher();
        setupListeners();

        // Offline-first: load cached profile from SQLite first
        loadCachedProfile();

        // Fetch fresh profile from API
        fetchProfileFromApi(false);
    }

    /**
     * Initializes UI element references.
     */
    private void initViews() {
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        tvAvatarInitials = findViewById(R.id.tvAvatarInitials);
        tvProfileName = findViewById(R.id.tvProfileName);
        tvProfileNic = findViewById(R.id.tvProfileNic);
        tvProfileStatus = findViewById(R.id.tvProfileStatus);
        tvProfileEmail = findViewById(R.id.tvProfileEmail);
        tvProfilePhone = findViewById(R.id.tvProfilePhone);
        tvProfileAddress = findViewById(R.id.tvProfileAddress);
        tvProfileMemberSince = findViewById(R.id.tvProfileMemberSince);

        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnDeactivateAccount = findViewById(R.id.btnDeactivateAccount);
        btnLogout = findViewById(R.id.btnLogout);
        progressBar = findViewById(R.id.progressBar);

        swipeRefreshLayout.setColorSchemeResources(R.color.primary);
    }

    /**
     * Sets up ActivityResultLauncher to refresh data after edit.
     */
    private void setupLauncher() {
        editProfileLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        fetchProfileFromApi(false);
                    }
                }
        );
    }

    /**
     * Sets up button click listeners and pull-to-refresh.
     */
    private void setupListeners() {
        swipeRefreshLayout.setOnRefreshListener(() -> fetchProfileFromApi(true));

        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            if (currentProfile != null) {
                intent.putExtra(EXTRA_PROFILE_NIC, currentProfile.getNic());
                intent.putExtra(EXTRA_PROFILE_NAME, currentProfile.getFullName());
                intent.putExtra(EXTRA_PROFILE_EMAIL, currentProfile.getEmail());
                intent.putExtra(EXTRA_PROFILE_PHONE, currentProfile.getPhone());
                intent.putExtra(EXTRA_PROFILE_ADDRESS, currentProfile.getAddress());
            } else {
                intent.putExtra(EXTRA_PROFILE_NIC, userNic);
                intent.putExtra(EXTRA_PROFILE_NAME, sessionManager.getUserName());
                intent.putExtra(EXTRA_PROFILE_EMAIL, sessionManager.getUserEmail());
            }
            editProfileLauncher.launch(intent);
        });

        btnDeactivateAccount.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, DeactivateAccountActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    /**
     * Loads locally cached profile data from SQLite.
     */
    private void loadCachedProfile() {
        if (userDao != null) {
            ProsumerProfile cached = userDao.getProsumerProfile(userNic);
            if (cached != null) {
                currentProfile = cached;
                populateProfileUI(cached);
            } else {
                // Populate what we have in SessionManager
                tvProfileNic.setText(getString(R.string.profile_label_nic) + ": " + userNic);
                tvProfileName.setText(sessionManager.getUserName());
                tvProfileEmail.setText(sessionManager.getUserEmail());
                tvAvatarInitials.setText(getInitials(sessionManager.getUserName()));
            }
        }
    }

    /**
     * Fetches current profile data from the C# Web API.
     *
     * @param isSwipeRefresh true if triggered by pull-to-refresh.
     */
    private void fetchProfileFromApi(boolean isSwipeRefresh) {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            if (isSwipeRefresh) {
                swipeRefreshLayout.setRefreshing(false);
            }
            if (currentProfile == null) {
                showSnackbar(getString(R.string.error_no_internet));
            }
            return;
        }

        if (!isSwipeRefresh) {
            progressBar.setVisibility(View.VISIBLE);
        }

        ApiService apiService = ApiClient.getApiService(this);
        apiService.getProfile(userNic).enqueue(new Callback<ProsumerProfile>() {
            @Override
            public void onResponse(@NonNull Call<ProsumerProfile> call,
                                   @NonNull Response<ProsumerProfile> response) {
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    currentProfile = response.body();
                    populateProfileUI(currentProfile);

                    // Update local caches
                    if (userDao != null) {
                        userDao.saveSession(currentProfile, sessionManager.getAuthToken());
                    }
                    sessionManager.updateUserProfile(
                            currentProfile.getFullName(),
                            currentProfile.getEmail()
                    );
                } else if (response.code() == 401) {
                    showSnackbar(getString(R.string.error_unauthorized));
                    redirectToLogin();
                } else {
                    showSnackbar(getString(R.string.label_error));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProsumerProfile> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                showSnackbar(getString(R.string.label_error) + ": " + t.getMessage());
            }
        });
    }

    /**
     * Populates UI controls with profile fields.
     */
    private void populateProfileUI(ProsumerProfile profile) {
        if (profile == null) return;

        tvProfileName.setText(profile.getFullName() != null ? profile.getFullName() : "-");
        tvProfileNic.setText(getString(R.string.profile_label_nic) + ": " + profile.getNic());
        tvProfileEmail.setText(profile.getEmail() != null ? profile.getEmail() : "-");
        tvProfilePhone.setText(profile.getPhone() != null && !profile.getPhone().isEmpty() ? profile.getPhone() : "Not provided");
        tvProfileAddress.setText(profile.getAddress() != null && !profile.getAddress().isEmpty() ? profile.getAddress() : "Not provided");

        // Format member since date
        if (profile.getCreatedAt() != null && !profile.getCreatedAt().isEmpty()) {
            tvProfileMemberSince.setText(DateTimeUtils.formatApiDateForDisplay(profile.getCreatedAt()));
        } else {
            tvProfileMemberSince.setText("-");
        }

        // Account status
        String status = profile.getStatus() != null ? profile.getStatus() : "Active";
        tvProfileStatus.setText(status);
        applyStatusBadgeStyle(status);

        // Avatar initials
        tvAvatarInitials.setText(getInitials(profile.getFullName()));
    }

    /**
     * Applies color styling to account status badge.
     */
    private void applyStatusBadgeStyle(String status) {
        if ("Active".equalsIgnoreCase(status)) {
            tvProfileStatus.setTextColor(getColor(R.color.status_success));
            tvProfileStatus.setBackgroundColor(getColor(R.color.status_success_light));
        } else if ("Pending".equalsIgnoreCase(status)) {
            tvProfileStatus.setTextColor(getColor(R.color.status_pending));
            tvProfileStatus.setBackgroundColor(getColor(R.color.status_pending_light));
        } else if ("Deactivated".equalsIgnoreCase(status)) {
            tvProfileStatus.setTextColor(getColor(R.color.status_error));
            tvProfileStatus.setBackgroundColor(getColor(R.color.status_error_light));
        }
    }

    /**
     * Generates a 1-2 character initials string for avatar display.
     */
    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "SS";
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    /**
     * Shows a confirmation dialog before logging out.
     */
    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_logout_title)
                .setMessage(R.string.dialog_logout_message)
                .setPositiveButton(R.string.action_logout, (dialog, which) -> {
                    SmartSolarApplication app = SmartSolarApplication.getInstance();
                    if (app != null) {
                        app.performLogout();
                    } else {
                        sessionManager.logout();
                    }
                    Toast.makeText(ProfileActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                    redirectToLogin();
                })
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    /**
     * Redirects user to LoginActivity and clears backstack.
     */
    private void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }
}
