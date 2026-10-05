package com.smartsolar.app.account;

import android.content.Intent;
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
 * then synchronizes with the C# Web API using NIC.
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
    private TextView tvProfileRole;
    private TextView tvProfileNicDetail;

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
        userDao = SmartSolarApplication.getInstance() != null
                ? SmartSolarApplication.getInstance().getUserDao()
                : null;

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
        tvProfileRole = findViewById(R.id.tvProfileRole);
        tvProfileNicDetail = findViewById(R.id.tvProfileNicDetail);

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
                });
    }

    /**
     * Sets up button click listeners and pull-to-refresh.
     */
    private void setupListeners() {
        swipeRefreshLayout.setOnRefreshListener(() -> fetchProfileFromApi(true));

        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            if (currentProfile != null) {
                intent.putExtra(EXTRA_PROFILE_NIC, getSafeString(currentProfile.getNic(), userNic));
                intent.putExtra(EXTRA_PROFILE_NAME, getSafeString(currentProfile.getFullName(), sessionManager.getUserName()));
                intent.putExtra(EXTRA_PROFILE_EMAIL, getSafeString(currentProfile.getEmail(), sessionManager.getUserEmail()));
                intent.putExtra(EXTRA_PROFILE_PHONE, getSafeString(currentProfile.getPhone(), ""));
                intent.putExtra(EXTRA_PROFILE_ADDRESS, getSafeString(currentProfile.getAddress(), ""));
            } else {
                intent.putExtra(EXTRA_PROFILE_NIC, userNic != null ? userNic : "");
                intent.putExtra(EXTRA_PROFILE_NAME, getSafeString(sessionManager.getUserName(), ""));
                intent.putExtra(EXTRA_PROFILE_EMAIL, getSafeString(sessionManager.getUserEmail(), ""));
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
        if (userDao != null && userNic != null) {
            ProsumerProfile cached = userDao.getProsumerProfile(userNic);
            if (cached != null) {
                currentProfile = cached;
                populateProfileUI(cached);
                return;
            }
        }

        // Fallback to SessionManager data if cache is unavailable
        String name = getSafeString(sessionManager.getUserName(), "Example Prosumer");
        String email = getSafeString(sessionManager.getUserEmail(), "prosumer@example.com");
        String nic = getSafeString(userNic, "199012345678");
        String role = getSafeString(sessionManager.getUserRole(), "Prosumer");

        tvProfileNic.setText(getString(R.string.profile_label_nic) + ": " + nic);
        tvProfileName.setText(name);
        tvProfileEmail.setText(email);
        tvProfileRole.setText(role);
        tvProfileNicDetail.setText(nic);
        tvAvatarInitials.setText(getInitials(name));
    }

    /**
     * Fetches current profile data from the C# Web API using user's NIC.
     *
     * @param isSwipeRefresh true if triggered by pull-to-refresh.
     */
    private void fetchProfileFromApi(boolean isSwipeRefresh) {
        if (userNic == null || userNic.isEmpty()) {
            if (isSwipeRefresh) swipeRefreshLayout.setRefreshing(false);
            showSnackbar("Invalid NIC session");
            return;
        }

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
                            getSafeString(currentProfile.getFullName(), sessionManager.getUserName()),
                            getSafeString(currentProfile.getEmail(), sessionManager.getUserEmail())
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
     * Populates UI controls with profile fields and handles null values gracefully.
     */
    private void populateProfileUI(ProsumerProfile profile) {
        if (profile == null) return;

        String name = getSafeString(profile.getFullName(), sessionManager.getUserName());
        String nic = getSafeString(profile.getNic(), userNic);
        String email = getSafeString(profile.getEmail(), sessionManager.getUserEmail());
        String phone = getSafeString(profile.getPhone(), "0761234567");
        String address = getSafeString(profile.getAddress(), "Colombo");
        String role = getSafeString(profile.getRole(), sessionManager.getUserRole());
        String status = getSafeString(profile.getStatus(), "Active");

        tvProfileName.setText(!name.isEmpty() ? name : "Example Prosumer");
        tvProfileNic.setText(getString(R.string.profile_label_nic) + ": " + (!nic.isEmpty() ? nic : "199012345678"));
        tvProfileEmail.setText(!email.isEmpty() ? email : "prosumer@example.com");
        tvProfilePhone.setText(!phone.isEmpty() ? phone : "0761234567");
        tvProfileAddress.setText(!address.isEmpty() ? address : "Colombo");

        tvProfileRole.setText(!role.isEmpty() ? role : "Prosumer");
        tvProfileNicDetail.setText(!nic.isEmpty() ? nic : "199012345678");

        // Format member since date
        if (profile.getCreatedAt() != null && !profile.getCreatedAt().trim().isEmpty()) {
            tvProfileMemberSince.setText(DateTimeUtils.formatApiDateForDisplay(profile.getCreatedAt()));
        } else {
            tvProfileMemberSince.setText("Not provided");
        }

        // Account status styling
        tvProfileStatus.setText(status);
        applyStatusBadgeStyle(status);

        // Avatar initials
        tvAvatarInitials.setText(getInitials(name));
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
            return "NA";
        }
        String[] parts = name.trim().split("\\s+");
        if (parts.length == 1) {
            return parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
        }
        return (parts[0].substring(0, 1) + parts[parts.length - 1].substring(0, 1)).toUpperCase();
    }

    /**
     * Helper to retrieve string value or fallback default if null/empty.
     */
    private String getSafeString(String value, String fallback) {
        if (value != null && !value.trim().isEmpty()) {
            return value.trim();
        }
        return fallback != null ? fallback.trim() : "";
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