 /*
         * Smart Solar Microgrid Trading System
         * VerifyTransactionActivity.java
         *
         * Member 4 - Grid Operator App + Integration
         *
         * Purpose:
         * Allows Grid Operators to verify prosumer transaction QR codes
         * against the central Web API and finalize the energy transfer.
         *
         * Responsibilities:
         * - Receive QR data from QrScannerActivity.
         * - Extract booking and prosumer information.
         * - Retrieve booking details from the Web API.
         * - Verify and finalize the transaction through the API.
         * - Update the local booking cache after successful completion.
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
 * Transaction verification screen for Grid Operators.
 *
 * The operator scans a prosumer QR code. The QR information is
 * displayed and then verified against the central Web API.
 * A successful verification finalizes the energy transfer.
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

        setContentView(
                R.layout.activity_verify_transaction
        );

        bookingCacheDao =
                SmartSolarApplication
                        .getInstance()
                        .getBookingCacheDao();

        /*
         * Receive the QR content from QrScannerActivity.
         */
        rawQrData =
                getIntent().getStringExtra(
                        Constants.EXTRA_QR_DATA
                );

        /*
         * Also support direct booking verification when
         * the booking ID is passed by another activity.
         */
        if (rawQrData == null
                || rawQrData.trim().isEmpty()) {

            rawQrData =
                    getIntent().getStringExtra(
                            Constants.EXTRA_BOOKING_ID
                    );
        }

        initViews();
        setupListeners();
        parseScannedQrData();
    }

    /**
     * Connect Java variables to the verification screen views.
     */
    private void initViews() {

        flVerifyIconContainer =
                findViewById(R.id.flVerifyIconContainer);

        tvVerifyIcon =
                findViewById(R.id.tvVerifyIcon);

        tvVerifyTitle =
                findViewById(R.id.tvVerifyTitle);

        tvVerifySubtitle =
                findViewById(R.id.tvVerifySubtitle);

        cardVerifyStatus =
                findViewById(R.id.cardVerifyStatus);

        tvStatusIcon =
                findViewById(R.id.tvStatusIcon);

        tvVerificationStatusMessage =
                findViewById(
                        R.id.tvVerificationStatusMessage
                );

        tvVerifyBookingId =
                findViewById(R.id.tvVerifyBookingId);

        tvVerifyProsumerNic =
                findViewById(R.id.tvVerifyProsumerNic);

        tvVerifyProsumerName =
                findViewById(R.id.tvVerifyProsumerName);

        tvVerifyNodeName =
                findViewById(R.id.tvVerifyNodeName);

        tvVerifyEnergyKwh =
                findViewById(R.id.tvVerifyEnergyKwh);

        tvVerifySlotDateTime =
                findViewById(R.id.tvVerifySlotDateTime);

        pbVerification =
                findViewById(R.id.pbVerification);

        btnFinalizeTransaction =
                findViewById(R.id.btnFinalizeTransaction);

        btnRejectTransaction =
                findViewById(R.id.btnRejectTransaction);

        btnScanAnother =
                findViewById(R.id.btnScanAnother);
    }

    /**
     * Configure verification screen actions.
     */
    private void setupListeners() {

        /*
         * Send the transaction to the central API
         * for verification and finalization.
         */
        btnFinalizeTransaction.setOnClickListener(
                v -> executeFinalizeTransaction()
        );

        /*
         * Allow the operator to reject the transaction
         * after confirmation.
         */
        btnRejectTransaction.setOnClickListener(
                v -> showRejectConfirmationDialog()
        );

        /*
         * Return to the QR scanner to process another
         * prosumer transaction.
         */
        btnScanAnother.setOnClickListener(v -> {

            startActivity(
                    new Intent(
                            this,
                            QrScannerActivity.class
                    )
            );

            finish();
        });
    }

    /**
     * Parse the QR payload into the transaction fields.
     *
     * Supported JSON format:
     * {
     *   "bookingId": "...",
     *   "prosumerNic": "...",
     *   "qrToken": "...",
     *   "nodeId": "...",
     *   "energyKwh": 10
     * }
     */
    private void parseScannedQrData() {

        if (rawQrData == null
                || rawQrData.trim().isEmpty()) {

            showVerificationFailure(
                    "No QR transaction data was received."
            );

            btnFinalizeTransaction.setEnabled(false);
            return;
        }

        rawQrData = rawQrData.trim();

        try {

            /*
             * Try to interpret the QR content as JSON.
             */
            JsonObject json =
                    new Gson().fromJson(
                            rawQrData,
                            JsonObject.class
                    );

            if (json == null) {
                throw new Exception(
                        "Invalid QR JSON"
                );
            }

            if (json.has("bookingId")
                    && !json.get("bookingId").isJsonNull()) {

                bookingId =
                        json.get("bookingId")
                                .getAsString()
                                .trim();
            }

            if (json.has("prosumerNic")
                    && !json.get("prosumerNic").isJsonNull()) {

                prosumerNic =
                        json.get("prosumerNic")
                                .getAsString()
                                .trim();
            }

            if (json.has("qrToken")
                    && !json.get("qrToken").isJsonNull()) {

                qrToken =
                        json.get("qrToken")
                                .getAsString()
                                .trim();
            }

            if (json.has("nodeId")
                    && !json.get("nodeId").isJsonNull()) {

                nodeId =
                        json.get("nodeId")
                                .getAsString()
                                .trim();
            }

            if (json.has("energyKwh")
                    && !json.get("energyKwh").isJsonNull()) {

                energyKwh =
                        json.get("energyKwh")
                                .getAsDouble();
            }

        } catch (Exception e) {

            /*
             * Support QR codes containing only a booking ID
             * or transaction token.
             */
            bookingId = rawQrData;
            qrToken = rawQrData;
        }

        /*
         * A valid transaction must contain at least a booking ID
         * or QR token before verification can continue.
         */
        if (bookingId.isEmpty()
                && qrToken.isEmpty()) {

            showVerificationFailure(
                    "Invalid transaction QR data."
            );

            btnFinalizeTransaction.setEnabled(false);
            return;
        }

        bindInitialData();
        loadBookingDetailsFromApi();
    }

    /**
     * Display the transaction information obtained from
     * the QR payload before server verification.
     */
    private void bindInitialData() {

        tvVerifyBookingId.setText(
                !bookingId.isEmpty()
                        ? bookingId
                        : "N/A"
        );

        tvVerifyProsumerNic.setText(
                !prosumerNic.isEmpty()
                        ? prosumerNic
                        : "N/A"
        );

        tvVerifyProsumerName.setText(
                !prosumerNic.isEmpty()
                        ? "Prosumer (" + prosumerNic + ")"
                        : "Prosumer"
        );

        tvVerifyNodeName.setText(
                !nodeId.isEmpty()
                        ? "Node: " + nodeId
                        : "Microgrid Station"
        );

        tvVerifyEnergyKwh.setText(
                energyKwh > 0
                        ? energyKwh + " kWh"
                        : "Pending Verification"
        );

        tvVerifySlotDateTime.setText(
                DateTimeUtils.formatDisplayDateTime(
                        new Date()
                )
        );
    }

    /**
     * Retrieve the latest booking information from
     * the central Web API.
     *
     * A locally cached booking is displayed first when available.
     */
    private void loadBookingDetailsFromApi() {

        if (bookingId.isEmpty()) {
            return;
        }

        /*
         * Display cached information immediately when available.
         */
        if (bookingCacheDao != null) {

            BookingResponse cached =
                    bookingCacheDao.getBookingById(
                            bookingId
                    );

            if (cached != null) {
                bindBookingDetails(cached);
            }
        }

        /*
         * Retrieve fresh information from the API.
         */
        if (!NetworkUtils.isNetworkAvailable(this)) {

            showSnackbar(
                    getString(
                            R.string.error_no_internet
                    )
            );

            return;
        }

        ApiService apiService =
                ApiClient.getApiService(this);

        apiService.getBookingById(bookingId)
                .enqueue(
                        new Callback<BookingResponse>() {

                            /**
                             * Process the latest booking
                             * information returned by the API.
                             */
                            @Override
                            public void onResponse(
                                    @NonNull Call<BookingResponse> call,
                                    @NonNull Response<BookingResponse> response
                            ) {

                                if (response.isSuccessful()
                                        && response.body() != null) {

                                    bindBookingDetails(
                                            response.body()
                                    );

                                } else {

                                    showSnackbar(
                                            "Unable to load booking details."
                                    );
                                }
                            }

                            /**
                             * Handle communication failure
                             * while loading the booking.
                             */
                            @Override
                            public void onFailure(
                                    @NonNull Call<BookingResponse> call,
                                    @NonNull Throwable throwable
                            ) {

                                showSnackbar(
                                        "Unable to connect to server."
                                );
                            }
                        }
                );
    }

    /**
     * Bind API booking information to the verification screen.
     */
    private void bindBookingDetails(
            BookingResponse booking
    ) {

        if (booking.getBookingId() != null) {

            bookingId =
                    booking.getBookingId();

            tvVerifyBookingId.setText(
                    bookingId
            );
        }

        if (booking.getProsumerNic() != null) {

            prosumerNic =
                    booking.getProsumerNic();

            tvVerifyProsumerNic.setText(
                    prosumerNic
            );

            tvVerifyProsumerName.setText(
                    "Prosumer (" + prosumerNic + ")"
            );
        }

        if (booking.getNodeName() != null) {

            tvVerifyNodeName.setText(
                    booking.getNodeName()
            );
        }

        if (booking.getEnergyKwh() > 0) {

            energyKwh =
                    booking.getEnergyKwh();

            tvVerifyEnergyKwh.setText(
                    energyKwh + " kWh"
            );
        }

        String dateStr =
                booking.getSlotDate() != null
                        ? booking.getSlotDate()
                        : "";

        Date parsedDate =
                DateTimeUtils.parseApiDate(
                        booking.getSlotDate()
                );

        if (parsedDate != null) {

            dateStr =
                    DateTimeUtils.formatDisplayDate(
                            parsedDate
                    );
        }

        String timeStr =
                booking.getSlotTime() != null
                        ? booking.getSlotTime()
                        : "";

        tvVerifySlotDateTime.setText(
                dateStr + " • " + timeStr
        );

        /*
         * Use the server QR token when available.
         */
        if (booking.getQrToken() != null
                && !booking.getQrToken().isEmpty()) {

            qrToken =
                    booking.getQrToken();
        }
    }

    /**
     * Send the transaction to the central Web API.
     *
     * The API is responsible for the actual business logic
     * and database update. The Android client only sends the
     * verification request and displays the result.
     */
    private void executeFinalizeTransaction() {

        /*
         * Check network connectivity first.
         */
        if (!NetworkUtils.isNetworkAvailable(this)) {

            showSnackbar(
                    getString(
                            R.string.error_no_internet
                    )
            );

            return;
        }

        /*
         * Do not send an incomplete verification request.
         */
        if (bookingId.isEmpty()) {

            showVerificationFailure(
                    "Booking ID is missing."
            );

            return;
        }

        if (prosumerNic.isEmpty()) {

            showVerificationFailure(
                    "Prosumer NIC is missing."
            );

            return;
        }

        if (qrToken.isEmpty()) {

            showVerificationFailure(
                    "QR token is missing."
            );

            return;
        }

        setLoading(true);

        /*
         * Build the verification request expected by
         * the central Web API.
         */
        Map<String, String> verificationData =
                new HashMap<>();

        verificationData.put(
                "bookingId",
                bookingId
        );

        verificationData.put(
                "prosumerNic",
                prosumerNic
        );

        verificationData.put(
                "qrToken",
                qrToken
        );

        ApiService apiService =
                ApiClient.getApiService(this);

        /*
         * The API performs server-side verification and
         * finalizes the energy transfer.
         */
        apiService
                .verifyAndFinalizeTransaction(
                        verificationData
                )
                .enqueue(
                        new Callback<BookingSummary>() {

                            /**
                             * Process the server verification result.
                             */
                            @Override
                            public void onResponse(
                                    @NonNull Call<BookingSummary> call,
                                    @NonNull Response<BookingSummary> response
                            ) {

                                setLoading(false);

                                if (response.isSuccessful()
                                        && response.body() != null) {

                                    showVerificationSuccess(
                                            response.body()
                                    );

                                } else {

                                    String errorMessage =
                                            parseErrorMessage(
                                                    response
                                            );

                                    showVerificationFailure(
                                            errorMessage
                                    );
                                }
                            }

                            /**
                             * Handle API/network communication failures.
                             */
                            @Override
                            public void onFailure(
                                    @NonNull Call<BookingSummary> call,
                                    @NonNull Throwable throwable
                            ) {

                                setLoading(false);

                                showVerificationFailure(
                                        "Verification connection failed."
                                );
                            }
                        }
                );
    }

    /**
     * Display successful server verification and finalization.
     */
    private void showVerificationSuccess(
            BookingSummary summary
    ) {

        cardVerifyStatus.setCardBackgroundColor(
                getColor(
                        R.color.status_success_light
                )
        );

        cardVerifyStatus.setStrokeColor(
                getColor(
                        R.color.status_success
                )
        );

        tvStatusIcon.setText("✅");

        tvVerificationStatusMessage.setText(
                summary.getMessage() != null
                        ? summary.getMessage()
                        : getString(
                        R.string.operator_verify_success
                )
        );

        tvVerificationStatusMessage.setTextColor(
                getColor(
                        R.color.status_success
                )
        );

        /*
         * Update the local cache after the server confirms
         * that the transaction has been completed.
         */
        if (bookingCacheDao != null
                && !bookingId.isEmpty()) {

            BookingResponse cached =
                    bookingCacheDao.getBookingById(
                            bookingId
                    );

            if (cached != null) {

                cached.setStatus(
                        Constants.STATUS_COMPLETED
                );

                bookingCacheDao.updateBooking(
                        cached
                );
            }
        }

        /*
         * Prevent the operator from finalizing the same
         * transaction twice from this screen.
         */
        btnFinalizeTransaction.setEnabled(false);

        btnFinalizeTransaction.setText(
                "Transaction Finalized"
        );

        btnRejectTransaction.setVisibility(
                View.GONE
        );

        Toast.makeText(
                this,
                R.string.operator_verify_success,
                Toast.LENGTH_LONG
        ).show();
    }

    /**
     * Display a failed verification result.
     */
    private void showVerificationFailure(
            String message
    ) {

        cardVerifyStatus.setCardBackgroundColor(
                getColor(
                        R.color.status_error_light
                )
        );

        cardVerifyStatus.setStrokeColor(
                getColor(
                        R.color.status_error
                )
        );

        tvStatusIcon.setText("❌");

        tvVerificationStatusMessage.setText(
                message != null
                        ? message
                        : getString(
                        R.string.operator_verify_failed
                )
        );

        tvVerificationStatusMessage.setTextColor(
                getColor(
                        R.color.status_error
                )
        );

        showSnackbar(
                message != null
                        ? message
                        : getString(
                        R.string.operator_verify_failed
                )
        );
    }

    /**
     * Ask the operator to confirm transaction rejection.
     */
    private void showRejectConfirmationDialog() {

        new MaterialAlertDialogBuilder(this)
                .setTitle(
                        R.string.operator_btn_reject
                )
                .setMessage(
                        "Are you sure you want to reject this energy transfer transaction?"
                )
                .setPositiveButton(
                        "Reject",
                        (dialog, which) -> {

                            showVerificationFailure(
                                    "Transaction was rejected by operator."
                            );

                            btnFinalizeTransaction.setEnabled(
                                    false
                            );

                            btnRejectTransaction.setEnabled(
                                    false
                            );
                        }
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .show();
    }

    /**
     * Extract a readable error message from the API response.
     */
    private String parseErrorMessage(
            Response<?> response
    ) {

        try {

            if (response.errorBody() != null) {

                String errorJson =
                        response.errorBody().string();

                ApiError apiError =
                        new Gson().fromJson(
                                errorJson,
                                ApiError.class
                        );

                if (apiError != null
                        && apiError.getMessage() != null
                        && !apiError.getMessage()
                        .trim()
                        .isEmpty()) {

                    return apiError.getMessage();
                }
            }

        } catch (Exception ignored) {
            // Use the default verification error below.
        }

        return getString(
                R.string.operator_verify_failed
        );
    }

    /**
     * Enable or disable controls while a server
     * verification request is being processed.
     */
    private void setLoading(
            boolean isLoading
    ) {

        pbVerification.setVisibility(
                isLoading
                        ? View.VISIBLE
                        : View.GONE
        );

        btnFinalizeTransaction.setEnabled(
                !isLoading
        );

        btnRejectTransaction.setEnabled(
                !isLoading
        );

        btnScanAnother.setEnabled(
                !isLoading
        );
    }

    /**
     * Display a Snackbar message on the verification screen.
     */
    private void showSnackbar(
            String message
    ) {

        Snackbar.make(
                findViewById(
                        android.R.id.content
                ),
                message,
                Snackbar.LENGTH_LONG
        ).show();
    }
}