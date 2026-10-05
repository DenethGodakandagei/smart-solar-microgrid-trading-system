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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
                Class<?> qrClass = Class.forName("com.smartsolar.app.qr.QrGeneratorActivity");
                Intent qrIntent = new Intent(this, qrClass);
                qrIntent.putExtra(Constants.EXTRA_BOOKING_ID, bookingId);
                qrIntent.putExtra(Constants.EXTRA_QR_DATA, qrToken != null ? qrToken : bookingId);
                startActivity(qrIntent);
            } catch (ClassNotFoundException e) {
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
        String status = normalizeStatus(intent.getStringExtra("status"));
        String message = intent.getStringExtra("message");
        qrToken = intent.getStringExtra("qr_token");

        configureActionAppearance(actionType, status, message);

        tvSummaryBookingId.setText(displayBookingId(bookingId));
        tvSummaryStatusBadge.setText(status);
        tvSummaryNodeName.setText(nodeName != null && !nodeName.isEmpty() ? nodeName : "Microgrid Node");
        tvSummaryDate.setText(formatApiDate(slotDate));
        tvSummaryTimeSlot.setText(slotTime != null && !slotTime.isEmpty() ? slotTime : "N/A");
        tvSummaryEnergyKwh.setText(energyKwh > 0
                ? String.format(Locale.getDefault(), "%.1f kWh", energyKwh) : "N/A");
        tvSummaryNic.setText(sessionManager.getUserNic());

        applyBadgeColor(status);

        if ((nodeName == null || nodeName.isEmpty()) && bookingId != null) {
            fetchMissingDetails();
        }
    }

    // ---------- helpers ----------

    /** Turns 0/1/2/3 (old numeric enum) into words; leaves real words untouched. */
    private String normalizeStatus(String s) {
        if (s == null || s.trim().isEmpty()) return "Pending";
        switch (s.trim()) {
            case "0": return "Pending";
            case "1": return "Confirmed";
            case "2": return "Cancelled";
            case "3": return "Completed";
            default:  return s.trim();
        }
    }

    /** Shows a short readable ID (BK-XXXXXXXX) instead of the 24-char Mongo id. */
    private String displayBookingId(String id) {
        if (id == null || id.isEmpty()) return "N/A";
        if (id.length() > 8) return "BK-" + id.substring(id.length() - 8).toUpperCase(Locale.US);
        return id;
    }

    /** Parses the server's UTC date and shows it in the phone's local time zone. */
    private String formatApiDate(String raw) {
        if (raw == null || raw.isEmpty()) return "N/A";

        String[] patterns = {"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd'T'HH:mm:ss'Z'"};
        for (String p : patterns) {
            try {
                SimpleDateFormat in = new SimpleDateFormat(p, Locale.US);
                in.setTimeZone(TimeZone.getTimeZone("UTC"));
                Date d = in.parse(raw);
                if (d != null) {
                    return new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(d);
                }
            } catch (ParseException ignored) {
            }
        }

        Date parsed = DateTimeUtils.parseApiDate(raw);
        return parsed != null ? DateTimeUtils.formatDisplayDate(parsed) : raw;
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
                btnViewQr.setVisibility("Cancelled".equalsIgnoreCase(status) ? View.GONE : View.VISIBLE);
                break;
        }
    }

    private void applyBadgeColor(String status) {
        if (status == null) status = "Pending";

        if (Constants.STATUS_APPROVED.equalsIgnoreCase(status) || "Confirmed".equalsIgnoreCase(status)) {
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
        if (b.getSlotDate() != null) tvSummaryDate.setText(formatApiDate(b.getSlotDate()));
        if (b.getSlotTime() != null) tvSummaryTimeSlot.setText(b.getSlotTime());
        if (b.getEnergyKwh() > 0) {
            tvSummaryEnergyKwh.setText(String.format(Locale.getDefault(), "%.1f kWh", b.getEnergyKwh()));
        }
        if (b.getStatus() != null) {
            String s = normalizeStatus(b.getStatus());
            tvSummaryStatusBadge.setText(s);
            applyBadgeColor(s);
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