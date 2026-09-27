/*
 * Smart Solar Microgrid Trading System
 * VerifyTransactionActivity.java
 *
 * Member 2 - Native Android Prosumer Application
 * Allows grid operators to verify prosumer QR transaction tokens
 * against the backend server and finalize the energy dispatch job.
 */
package com.smartsolar.app.operator;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.smartsolar.app.R;
import com.smartsolar.app.SmartSolarApplication;
import com.smartsolar.app.api.ApiClient;
import com.smartsolar.app.api.ApiService;
import com.smartsolar.app.api.models.ApiError;
import com.smartsolar.app.api.models.BookingResponse;
import com.smartsolar.app.api.models.BookingSummary;
import com.smartsolar.app.db.BookingCacheDao;
import com.smartsolar.app.qr.QrScannerActivity;
import com.smartsolar.app.utils.Constants;
import com.smartsolar.app.utils.DateTimeUtils;
import com.smartsolar.app.utils.NetworkUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Verifies scanned transaction QR data against server /api/operator/verify
 * and completes the energy transfer job.
 */
public class VerifyTransactionActivity extends AppCompatActivity {

    private FrameLayout flVerifyIconContainer;
    private TextView tvVerifyIcon;
    private TextView tvVerifyTitle;
    private TextView tvVerifySubtitle;

    private MaterialCardView cardVerifyStatus;
    private TextView tvStatusIcon;
    private TextView tvVerificationStatusMessage;

    private TextView tvVerifyBookingId;
    private TextView tvVerifyProsumerNic;
    private TextView tvVerifyProsumerName;
    private TextView tvVerifyNodeName;
    private TextView tvVerifyEnergyKwh;
    private TextView tvVerifySlotDateTime;

    private ProgressBar pbVerification;
    private MaterialButton btnFinalizeTransaction;
    private MaterialButton btnRejectTransaction;
    private MaterialButton btnScanAnother;

    private BookingCacheDao bookingCacheDao;

    private String rawQrData;
    private String bookingId = "";
    private String prosumerNic = "";
    private String qrToken = "";
    private String nodeId = "";
    private double energyKwh = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_transaction);

        bookingCacheDao = SmartSolarApplication.getInstance().getBookingCacheDao();

        rawQrData = getIntent().getStringExtra(Constants.EXTRA_QR_DATA);
        if (rawQrData == null || rawQrData.isEmpty()) {
            rawQrData = getIntent().getStringExtra(Constants.EXTRA_BOOKING_ID);
        }

        initViews();
        setupListeners();
        parseScannedQrData();
    }

    private void initViews() {
        flVerifyIconContainer = findViewById(R.id.flVerifyIconContainer);
        tvVerifyIcon = findViewById(R.id.tvVerifyIcon);
        tvVerifyTitle = findViewById(R.id.tvVerifyTitle);
        tvVerifySubtitle = findViewById(R.id.tvVerifySubtitle);

        cardVerifyStatus = findViewById(R.id.cardVerifyStatus);
        tvStatusIcon = findViewById(R.id.tvStatusIcon);
        tvVerificationStatusMessage = findViewById(R.id.tvVerificationStatusMessage);

        tvVerifyBookingId = findViewById(R.id.tvVerifyBookingId);
        tvVerifyProsumerNic = findViewById(R.id.tvVerifyProsumerNic);
        tvVerifyProsumerName = findViewById(R.id.tvVerifyProsumerName);
        tvVerifyNodeName = findViewById(R.id.tvVerifyNodeName);
        tvVerifyEnergyKwh = findViewById(R.id.tvVerifyEnergyKwh);
        tvVerifySlotDateTime = findViewById(R.id.tvVerifySlotDateTime);

        pbVerification = findViewById(R.id.pbVerification);
        btnFinalizeTransaction = findViewById(R.id.btnFinalizeTransaction);
        btnRejectTransaction = findViewById(R.id.btnRejectTransaction);
        btnScanAnother = findViewById(R.id.btnScanAnother);
    }

    private void setupListeners() {
        btnFinalizeTransaction.setOnClickListener(v -> executeFinalizeTransaction());

        btnRejectTransaction.setOnClickListener(v -> showRejectConfirmationDialog());

        btnScanAnother.setOnClickListener(v -> {
            startActivity(new Intent(this, QrScannerActivity.class));
            finish();
        });
    }

    /**
     * Parses the QR string payload into booking metadata.
     */
    private void parseScannedQrData() {
        if (rawQrData == null || rawQrData.isEmpty()) {
            showSnackbar("No QR data received");
            return;
        }

        try {
            // Attempt to parse JSON payload
            JsonObject json = new Gson().fromJson(rawQrData, JsonObject.class);
            if (json.has("bookingId")) bookingId = json.get("bookingId").getAsString();
            if (json.has("prosumerNic")) prosumerNic = json.get("prosumerNic").getAsString();
            if (json.has("qrToken")) qrToken = json.get("qrToken").getAsString();
            if (json.has("nodeId")) nodeId = json.get("nodeId").getAsString();
            if (json.has("energyKwh")) energyKwh = json.get("energyKwh").getAsDouble();
        } catch (Exception e) {
            // Raw token or Booking ID format
            bookingId = rawQrData.trim();
            qrToken = rawQrData.trim();
        }

        bindInitialData();
        loadBookingDetailsFromApi();
    }

    private void bindInitialData() {
        tvVerifyBookingId.setText(bookingId != null && !bookingId.isEmpty() ? bookingId : "N/A");
        tvVerifyProsumerNic.setText(prosumerNic != null && !prosumerNic.isEmpty() ? prosumerNic : "N/A");
        tvVerifyProsumerName.setText("Prosumer (" + prosumerNic + ")");
        tvVerifyNodeName.setText(!nodeId.isEmpty() ? ("Node: " + nodeId) : "Microgrid Station");
        tvVerifyEnergyKwh.setText(energyKwh > 0 ? (energyKwh + " kWh") : "Pending Verification");
        tvVerifySlotDateTime.setText(DateTimeUtils.formatDisplayDateTime(new Date()));
    }

    private void loadBookingDetailsFromApi() {
        if (bookingId.isEmpty()) return;

        // Check local cache first
        if (bookingCacheDao != null) {
            BookingResponse cached = bookingCacheDao.getBookingById(bookingId);
            if (cached != null) {
                bindBookingDetails(cached);
            }
        }

        // Fetch fresh details from API
        if (NetworkUtils.isNetworkAvailable(this)) {
            ApiService apiService = ApiClient.getApiService(this);
            apiService.getBookingById(bookingId).enqueue(new Callback<BookingResponse>() {
                @Override
                public void onResponse(@NonNull Call<BookingResponse> call,
                                       @NonNull Response<BookingResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        bindBookingDetails(response.body());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<BookingResponse> call, @NonNull Throwable t) {}
            });
        }
    }

    private void bindBookingDetails(BookingResponse b) {
        if (b.getBookingId() != null) tvVerifyBookingId.setText(b.getBookingId());
        if (b.getProsumerNic() != null) {
            prosumerNic = b.getProsumerNic();
            tvVerifyProsumerNic.setText(prosumerNic);
            tvVerifyProsumerName.setText("Prosumer (" + prosumerNic + ")");
        }
        if (b.getNodeName() != null) tvVerifyNodeName.setText(b.getNodeName());
        if (b.getEnergyKwh() > 0) tvVerifyEnergyKwh.setText(b.getEnergyKwh() + " kWh");

        String dateStr = b.getSlotDate() != null ? b.getSlotDate() : "";
        Date parsed = DateTimeUtils.parseApiDate(b.getSlotDate());
        if (parsed != null) {
            dateStr = DateTimeUtils.formatDisplayDate(parsed);
        }
        String timeStr = b.getSlotTime() != null ? b.getSlotTime() : "";
        tvVerifySlotDateTime.setText(dateStr + " • " + timeStr);

        if (b.getQrToken() != null && !b.getQrToken().isEmpty()) {
            qrToken = b.getQrToken();
        }
    }

    private void executeFinalizeTransaction() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            showSnackbar(getString(R.string.error_no_internet));
            return;
        }

        setLoading(true);

        Map<String, String> verificationData = new HashMap<>();
        verificationData.put("bookingId", bookingId);
        verificationData.put("prosumerNic", prosumerNic);
        verificationData.put("qrToken", qrToken.isEmpty() ? bookingId : qrToken);

        ApiService apiService = ApiClient.getApiService(this);
        apiService.verifyAndFinalizeTransaction(verificationData).enqueue(new Callback<BookingSummary>() {
            @Override
            public void onResponse(@NonNull Call<BookingSummary> call,
                                   @NonNull Response<BookingSummary> response) {
                setLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    BookingSummary summary = response.body();
                    showVerificationSuccess(summary);
                } else {
                    String errorMsg = parseErrorMessage(response);
                    showVerificationFailure(errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<BookingSummary> call, @NonNull Throwable t) {
                setLoading(false);
                showVerificationFailure("Verification connection failed: " + t.getMessage());
            }
        });
    }

    private void showVerificationSuccess(BookingSummary summary) {
        // Update status banner
        cardVerifyStatus.setCardBackgroundColor(getColor(R.color.status_success_light));
        cardVerifyStatus.setStrokeColor(getColor(R.color.status_success));
        tvStatusIcon.setText("✅");
        tvVerificationStatusMessage.setText(summary.getMessage() != null ?
                summary.getMessage() : getString(R.string.operator_verify_success));
        tvVerificationStatusMessage.setTextColor(getColor(R.color.status_success));

        // Update local cache status
        if (bookingCacheDao != null && !bookingId.isEmpty()) {
            BookingResponse cached = bookingCacheDao.getBookingById(bookingId);
            if (cached != null) {
                cached.setStatus(Constants.STATUS_COMPLETED);
                bookingCacheDao.updateBooking(cached);
            }
        }

        btnFinalizeTransaction.setEnabled(false);
        btnFinalizeTransaction.setText("Transaction Finalized");
        btnRejectTransaction.setVisibility(View.GONE);

        Toast.makeText(this, R.string.operator_verify_success, Toast.LENGTH_LONG).show();
    }

    private void showVerificationFailure(String message) {
        cardVerifyStatus.setCardBackgroundColor(getColor(R.color.status_error_light));
        cardVerifyStatus.setStrokeColor(getColor(R.color.status_error));
        tvStatusIcon.setText("❌");
        tvVerificationStatusMessage.setText(message != null ? message : getString(R.string.operator_verify_failed));
        tvVerificationStatusMessage.setTextColor(getColor(R.color.status_error));

        showSnackbar(message != null ? message : getString(R.string.operator_verify_failed));
    }

    private void showRejectConfirmationDialog() {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.operator_btn_reject)
                .setMessage("Are you sure you want to reject this energy transfer transaction?")
                .setPositiveButton("Reject", (dialog, which) -> {
                    showVerificationFailure("Transaction was rejected by operator.");
                    btnFinalizeTransaction.setEnabled(false);
                    btnRejectTransaction.setEnabled(false);
                })
                .setNegativeButton("Cancel", null)
                .show();
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
        } catch (Exception ignored) {}
        return getString(R.string.operator_verify_failed);
    }

    private void setLoading(boolean isLoading) {
        pbVerification.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnFinalizeTransaction.setEnabled(!isLoading);
        btnRejectTransaction.setEnabled(!isLoading);
        btnScanAnother.setEnabled(!isLoading);
    }

    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }
}
