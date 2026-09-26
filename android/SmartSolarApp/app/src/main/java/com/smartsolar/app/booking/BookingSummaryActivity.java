/*
 * Smart Solar Microgrid Trading System
 * BookingSummaryActivity.java
 *
 * Member 2 - Native Android Prosumer Application
 * Displays a comprehensive summary card after any booking action
 * (Create, Update, or Cancel) and provides quick navigation.
 */
package com.smartsolar.app.booking;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.smartsolar.app.R;
import com.smartsolar.app.SmartSolarApplication;
import com.smartsolar.app.api.ApiClient;
import com.smartsolar.app.api.ApiService;
import com.smartsolar.app.api.models.BookingResponse;
import com.smartsolar.app.auth.SessionManager;
import com.smartsolar.app.dashboard.DashboardActivity;
import com.smartsolar.app.db.BookingCacheDao;
import com.smartsolar.app.utils.Constants;
import com.smartsolar.app.utils.DateTimeUtils;
import com.smartsolar.app.utils.NetworkUtils;

import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Summary page presented after a reservation is created, modified, or cancelled.
 * Shows confirmation ID, node, timing, energy kWh, status badge, and next actions.
 */
public class BookingSummaryActivity extends AppCompatActivity {

    private FrameLayout flStatusIconContainer;
    private TextView tvStatusIcon;
    private TextView tvSummaryTitle;
    private TextView tvSummarySubtitle;

    private TextView tvSummaryBookingId;
    private TextView tvSummaryStatusBadge;
    private TextView tvSummaryNodeName;
    private TextView tvSummaryDate;
    private TextView tvSummaryTimeSlot;
    private TextView tvSummaryEnergyKwh;
    private TextView tvSummaryNic;

    private MaterialCardView cardInstruction;
    private TextView tvInstructionIcon;
    private TextView tvInstructionMessage;

    private MaterialButton btnViewQr;
    private MaterialButton btnDone;

    private SessionManager sessionManager;
    private BookingCacheDao bookingCacheDao;

    private String bookingId;
    private String actionType;
    private String qrToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_summary);

        sessionManager = SessionManager.getInstance(this);
        bookingCacheDao = SmartSolarApplication.getInstance().getBookingCacheDao();

        initViews();
        setupListeners();
        populateDataFromIntent();
    }

    private void initViews() {
        flStatusIconContainer = findViewById(R.id.flStatusIconContainer);
        tvStatusIcon = findViewById(R.id.tvStatusIcon);
        tvSummaryTitle = findViewById(R.id.tvSummaryTitle);
        tvSummarySubtitle = findViewById(R.id.tvSummarySubtitle);

        tvSummaryBookingId = findViewById(R.id.tvSummaryBookingId);
        tvSummaryStatusBadge = findViewById(R.id.tvSummaryStatusBadge);
        tvSummaryNodeName = findViewById(R.id.tvSummaryNodeName);
        tvSummaryDate = findViewById(R.id.tvSummaryDate);
        tvSummaryTimeSlot = findViewById(R.id.tvSummaryTimeSlot);
        tvSummaryEnergyKwh = findViewById(R.id.tvSummaryEnergyKwh);
        tvSummaryNic = findViewById(R.id.tvSummaryNic);

        cardInstruction = findViewById(R.id.cardInstruction);
        tvInstructionIcon = findViewById(R.id.tvInstructionIcon);
        tvInstructionMessage = findViewById(R.id.tvInstructionMessage);

        btnViewQr = findViewById(R.id.btnViewQr);
        btnDone = findViewById(R.id.btnDone);
    }

    private void setupListeners() {
        btnDone.setOnClickListener(v -> navigateToDashboard());

        btnViewQr.setOnClickListener(v -> {
            try {
                // Dynamically navigate to QrGeneratorActivity
                Class<?> qrClass = Class.forName("com.smartsolar.app.qr.QrGeneratorActivity");
                Intent qrIntent = new Intent(this, qrClass);
                qrIntent.putExtra(Constants.EXTRA_BOOKING_ID, bookingId);
                qrIntent.putExtra(Constants.EXTRA_QR_DATA, qrToken != null ? qrToken : bookingId);
                startActivity(qrIntent);
            } catch (ClassNotFoundException e) {
                // If QR class not yet compiled, navigate to Dashboard
                navigateToDashboard();
            }
        });
    }

    private void populateDataFromIntent() {
        Intent intent = getIntent();
        bookingId = intent.getStringExtra(Constants.EXTRA_BOOKING_ID);
        actionType = intent.getStringExtra(Constants.EXTRA_ACTION_TYPE);
        if (actionType == null) actionType = "VIEW";

        String nodeName = intent.getStringExtra("node_name");
        String slotDate = intent.getStringExtra("slot_date");
        String slotTime = intent.getStringExtra("slot_time");
        double energyKwh = intent.getDoubleExtra("energy_kwh", 0.0);
        String status = intent.getStringExtra("status");
        String message = intent.getStringExtra("message");
        qrToken = intent.getStringExtra("qr_token");

        // Format Date for display
        String formattedDate = slotDate != null ? slotDate : "N/A";
        Date parsed = DateTimeUtils.parseApiDate(slotDate);
        if (parsed != null) {
            formattedDate = DateTimeUtils.formatDisplayDate(parsed);
        }

        // Apply Action Type Stylings
        configureActionAppearance(actionType, status, message);

        // Bind Data
        tvSummaryBookingId.setText(bookingId != null ? bookingId : "N/A");
        tvSummaryStatusBadge.setText(status != null ? status : Constants.STATUS_PENDING);
        tvSummaryNodeName.setText(nodeName != null && !nodeName.isEmpty() ? nodeName : "Microgrid Node");
        tvSummaryDate.setText(formattedDate);
        tvSummaryTimeSlot.setText(slotTime != null ? slotTime : "N/A");
        tvSummaryEnergyKwh.setText(energyKwh > 0 ? (energyKwh + " kWh") : "N/A");
        tvSummaryNic.setText(sessionManager.getUserNic());

        // Update badge color
        applyBadgeColor(status);

        // Fetch latest details from cache / API if some fields were omitted
        if (nodeName == null && bookingId != null) {
            fetchMissingDetails();
        }
    }

    private void configureActionAppearance(String action, String status, String customMessage) {
        switch (action.toUpperCase()) {
            case "CREATE":
                tvSummaryTitle.setText("Booking Confirmed!");
                tvSummarySubtitle.setText(customMessage != null ? customMessage : "Your energy slot reservation has been created");
                tvStatusIcon.setText("✓");
                flStatusIconContainer.setBackgroundResource(R.drawable.gradient_header);
                tvInstructionIcon.setText("💡");
                tvInstructionMessage.setText("Present your transaction QR code to the microgrid operator at the node to complete transfer.");
                btnViewQr.setVisibility(View.VISIBLE);
                break;

            case "UPDATE":
                tvSummaryTitle.setText("Booking Updated!");
                tvSummarySubtitle.setText(customMessage != null ? customMessage : "Your energy slot reservation has been updated");
                tvStatusIcon.setText("✎");
                flStatusIconContainer.setBackgroundResource(R.drawable.btn_primary);
                tvInstructionIcon.setText("ℹ️");
                tvInstructionMessage.setText("Your updated slot time and details are recorded. Show your updated QR code during the slot.");
                btnViewQr.setVisibility(View.VISIBLE);
                break;

            case "CANCEL":
                tvSummaryTitle.setText("Booking Cancelled");
                tvSummarySubtitle.setText(customMessage != null ? customMessage : "Your reservation has been cancelled successfully");
                tvStatusIcon.setText("✕");
                flStatusIconContainer.setBackgroundResource(R.drawable.btn_danger);
                cardInstruction.setCardBackgroundColor(getColor(R.color.status_error_light));
                cardInstruction.setStrokeColor(getColor(R.color.status_error));
                tvInstructionIcon.setText("🚫");
                tvInstructionMessage.setText("This energy slot has been released back into the microgrid pool.");
                tvInstructionMessage.setTextColor(getColor(R.color.status_error));
                btnViewQr.setVisibility(View.GONE);
                break;

            default:
                tvSummaryTitle.setText("Booking Summary");
                tvSummarySubtitle.setText(customMessage != null ? customMessage : "Reservation details");
                tvStatusIcon.setText("✓");
                btnViewQr.setVisibility(Constants.STATUS_CANCELLED.equalsIgnoreCase(status) ? View.GONE : View.VISIBLE);
                break;
        }
    }

    private void applyBadgeColor(String status) {
        if (status == null) status = Constants.STATUS_PENDING;

        if (Constants.STATUS_APPROVED.equalsIgnoreCase(status)) {
            tvSummaryStatusBadge.setTextColor(getColor(R.color.badge_approved));
        } else if (Constants.STATUS_CANCELLED.equalsIgnoreCase(status)) {
            tvSummaryStatusBadge.setTextColor(getColor(R.color.badge_cancelled));
        } else if (Constants.STATUS_COMPLETED.equalsIgnoreCase(status)) {
            tvSummaryStatusBadge.setTextColor(getColor(R.color.badge_completed));
        } else {
            tvSummaryStatusBadge.setTextColor(getColor(R.color.badge_pending));
        }
    }

    private void fetchMissingDetails() {
        if (bookingCacheDao != null) {
            BookingResponse cached = bookingCacheDao.getBookingById(bookingId);
            if (cached != null) {
                bindBookingResponse(cached);
                return;
            }
        }

        if (NetworkUtils.isNetworkAvailable(this)) {
            ApiService apiService = ApiClient.getApiService(this);
            apiService.getBookingById(bookingId).enqueue(new Callback<BookingResponse>() {
                @Override
                public void onResponse(@NonNull Call<BookingResponse> call,
                                       @NonNull Response<BookingResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        bindBookingResponse(response.body());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<BookingResponse> call, @NonNull Throwable t) {
                }
            });
        }
    }

    private void bindBookingResponse(BookingResponse b) {
        if (b == null) return;
        if (b.getNodeName() != null) tvSummaryNodeName.setText(b.getNodeName());
        if (b.getSlotDate() != null) {
            Date parsed = DateTimeUtils.parseApiDate(b.getSlotDate());
            tvSummaryDate.setText(parsed != null ? DateTimeUtils.formatDisplayDate(parsed) : b.getSlotDate());
        }
        if (b.getSlotTime() != null) tvSummaryTimeSlot.setText(b.getSlotTime());
        if (b.getEnergyKwh() > 0) tvSummaryEnergyKwh.setText(b.getEnergyKwh() + " kWh");
        if (b.getStatus() != null) {
            tvSummaryStatusBadge.setText(b.getStatus());
            applyBadgeColor(b.getStatus());
        }
        if (b.getQrToken() != null) {
            qrToken = b.getQrToken();
        }
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(this, DashboardActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        navigateToDashboard();
    }
}
