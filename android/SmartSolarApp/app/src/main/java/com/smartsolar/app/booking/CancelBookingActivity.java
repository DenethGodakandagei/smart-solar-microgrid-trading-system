/*
 * Smart Solar Microgrid Trading System
 * CancelBookingActivity.java
 *
 * Member 2 - Native Android Prosumer Application
 * Handles energy slot reservation cancellation with confirmation.
 * Enforces the 12-hour minimum notice business rule.
 */
package com.smartsolar.app.booking;

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
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.Gson;
import com.smartsolar.app.R;
import com.smartsolar.app.SmartSolarApplication;
import com.smartsolar.app.api.ApiClient;
import com.smartsolar.app.api.ApiService;
import com.smartsolar.app.api.models.ApiError;
import com.smartsolar.app.api.models.BookingResponse;
import com.smartsolar.app.auth.LoginActivity;
import com.smartsolar.app.auth.SessionManager;
import com.smartsolar.app.db.BookingCacheDao;
import com.smartsolar.app.utils.Constants;
import com.smartsolar.app.utils.DateTimeUtils;
import com.smartsolar.app.utils.NetworkUtils;

import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Screen where a prosumer confirms cancellation of a scheduled booking.
 * - Displays full reservation breakdown.
 * - Checks 12-hour notice policy before cancellation.
 * - Executes DELETE /api/bookings/{id} and routes to BookingSummaryActivity.
 */
public class CancelBookingActivity extends AppCompatActivity {

    private MaterialCardView cardWarningNotice;
    private TextView tvWarningIcon;
    private TextView tvWarningText;

    private TextView tvBookingId;
    private TextView tvStatusBadge;
    private TextView tvNodeName;
    private TextView tvSlotDate;
    private TextView tvSlotTime;
    private TextView tvEnergyKwh;
    private TextInputEditText etReason;

    private ProgressBar progressBar;
    private MaterialButton btnConfirmCancel;
    private MaterialButton btnKeepBooking;

    private SessionManager sessionManager;
    private BookingCacheDao bookingCacheDao;

    private String bookingId;
    private BookingResponse currentBooking;
    private boolean isNoticeRuleSatisfied = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cancel_booking);

        sessionManager = SessionManager.getInstance(this);
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        bookingCacheDao = SmartSolarApplication.getInstance().getBookingCacheDao();

        bookingId = getIntent().getStringExtra(Constants.EXTRA_BOOKING_ID);
        if (bookingId == null || bookingId.isEmpty()) {
            Toast.makeText(this, "Invalid booking identifier", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupListeners();
        loadBookingDetails();
    }

    private void initViews() {
        cardWarningNotice = findViewById(R.id.cardWarningNotice);
        tvWarningIcon = findViewById(R.id.tvWarningIcon);
        tvWarningText = findViewById(R.id.tvWarningText);

        tvBookingId = findViewById(R.id.tvBookingId);
        tvStatusBadge = findViewById(R.id.tvStatusBadge);
        tvNodeName = findViewById(R.id.tvNodeName);
        tvSlotDate = findViewById(R.id.tvSlotDate);
        tvSlotTime = findViewById(R.id.tvSlotTime);
        tvEnergyKwh = findViewById(R.id.tvEnergyKwh);
        etReason = findViewById(R.id.etReason);

        progressBar = findViewById(R.id.progressBar);
        btnConfirmCancel = findViewById(R.id.btnConfirmCancel);
        btnKeepBooking = findViewById(R.id.btnKeepBooking);

        tvBookingId.setText(bookingId);
    }

    private void setupListeners() {
        btnConfirmCancel.setOnClickListener(v -> showConfirmationDialog());
        btnKeepBooking.setOnClickListener(v -> finish());
    }

    private void loadBookingDetails() {
        // Load from local SQLite cache first
        if (bookingCacheDao != null) {
            BookingResponse cached = bookingCacheDao.getBookingById(bookingId);
            if (cached != null) {
                currentBooking = cached;
                populateViews(cached);
            }
        }

        // Fetch fresh from API
        if (NetworkUtils.isNetworkAvailable(this)) {
            setLoading(true);
            ApiService apiService = ApiClient.getApiService(this);
            apiService.getBookingById(bookingId).enqueue(new Callback<BookingResponse>() {
                @Override
                public void onResponse(@NonNull Call<BookingResponse> call,
                                       @NonNull Response<BookingResponse> response) {
                    setLoading(false);
                    if (response.isSuccessful() && response.body() != null) {
                        currentBooking = response.body();
                        if (bookingCacheDao != null) {
                            bookingCacheDao.insertBooking(currentBooking);
                        }
                        populateViews(currentBooking);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<BookingResponse> call, @NonNull Throwable t) {
                    setLoading(false);
                    if (currentBooking == null) {
                        showSnackbar("Failed to load booking details: " + t.getMessage());
                    }
                }
            });
        }
    }

    private void populateViews(BookingResponse booking) {
        tvBookingId.setText(booking.getBookingId() != null ? booking.getBookingId() : bookingId);
        tvStatusBadge.setText(booking.getStatus() != null ? booking.getStatus() : Constants.STATUS_PENDING);
        tvNodeName.setText(booking.getNodeName() != null ? booking.getNodeName() : "Grid Node (" + booking.getNodeId() + ")");

        // Date & notice validation
        Date slotDate = DateTimeUtils.parseApiDate(booking.getSlotDate());
        if (slotDate != null) {
            tvSlotDate.setText(DateTimeUtils.formatDisplayDate(slotDate));
            isNoticeRuleSatisfied = DateTimeUtils.hasMinimum12HourNotice(slotDate);
        } else {
            tvSlotDate.setText(booking.getSlotDate() != null ? booking.getSlotDate() : "N/A");
            isNoticeRuleSatisfied = true;
        }

        tvSlotTime.setText(booking.getSlotTime() != null ? booking.getSlotTime() : "N/A");
        tvEnergyKwh.setText(booking.getEnergyKwh() + " kWh");

        if (!isNoticeRuleSatisfied) {
            cardWarningNotice.setCardBackgroundColor(getColor(R.color.status_error_light));
            cardWarningNotice.setStrokeColor(getColor(R.color.status_error));
            tvWarningIcon.setText("🚨");
            tvWarningText.setText("Notice rule warning: Less than 12 hours remaining before this scheduled slot. Cancellation policy may reject this request.");
            tvWarningText.setTextColor(getColor(R.color.status_error));
        }
    }

    private void showConfirmationDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.booking_cancel_title)
                .setMessage(R.string.booking_cancel_confirm)
                .setPositiveButton(R.string.action_confirm, (dialog, which) -> executeCancelBooking())
                .setNegativeButton(R.string.action_cancel, null)
                .show();
    }

    private void executeCancelBooking() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            showSnackbar(getString(R.string.error_no_internet));
            return;
        }

        setLoading(true);
        ApiService apiService = ApiClient.getApiService(this);
        apiService.cancelBooking(bookingId).enqueue(new Callback<BookingResponse>() {
            @Override
            public void onResponse(@NonNull Call<BookingResponse> call,
                                   @NonNull Response<BookingResponse> response) {
                setLoading(false);

                if (response.isSuccessful()) {
                    BookingResponse result = response.body();

                    // Update cache status to Cancelled
                    if (bookingCacheDao != null) {
                        if (currentBooking != null) {
                            currentBooking.setStatus(Constants.STATUS_CANCELLED);
                            bookingCacheDao.updateBooking(currentBooking);
                        } else if (result != null) {
                            result.setStatus(Constants.STATUS_CANCELLED);
                            bookingCacheDao.updateBooking(result);
                        }
                    }

                    Toast.makeText(CancelBookingActivity.this,
                            R.string.booking_cancel_success, Toast.LENGTH_SHORT).show();

                    // Navigate to Summary Screen
                    Intent summaryIntent = new Intent(CancelBookingActivity.this, BookingSummaryActivity.class);
                    summaryIntent.putExtra(Constants.EXTRA_BOOKING_ID, bookingId);
                    summaryIntent.putExtra(Constants.EXTRA_ACTION_TYPE, "CANCEL");
                    summaryIntent.putExtra("node_name", currentBooking != null ? currentBooking.getNodeName() : "");
                    summaryIntent.putExtra("slot_date", currentBooking != null ? currentBooking.getSlotDate() : "");
                    summaryIntent.putExtra("slot_time", currentBooking != null ? currentBooking.getSlotTime() : "");
                    summaryIntent.putExtra("energy_kwh", currentBooking != null ? currentBooking.getEnergyKwh() : 0.0);
                    summaryIntent.putExtra("status", Constants.STATUS_CANCELLED);
                    summaryIntent.putExtra("message", result != null ? result.getMessage() : "Booking cancelled successfully.");

                    startActivity(summaryIntent);
                    finish();
                } else {
                    String errorMessage = parseErrorMessage(response);
                    showSnackbar(errorMessage);
                }
            }

            @Override
            public void onFailure(@NonNull Call<BookingResponse> call, @NonNull Throwable t) {
                setLoading(false);
                showSnackbar(getString(R.string.booking_cancel_failed) + ": " + t.getMessage());
            }
        });
    }

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
        return getString(R.string.booking_cancel_failed);
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnConfirmCancel.setEnabled(!isLoading);
        btnKeepBooking.setEnabled(!isLoading);
        etReason.setEnabled(!isLoading);
    }

    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }
}
