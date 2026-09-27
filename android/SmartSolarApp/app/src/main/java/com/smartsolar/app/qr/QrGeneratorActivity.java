/*
 * Smart Solar Microgrid Trading System
 * QrGeneratorActivity.java
 *
 * Member 2 - Native Android Prosumer Application
 * Generates and displays a secure transaction QR code using ZXing
 * for prosumer energy transfer authorization.
 */
package com.smartsolar.app.qr;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.gson.Gson;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.smartsolar.app.R;
import com.smartsolar.app.SmartSolarApplication;
import com.smartsolar.app.api.ApiClient;
import com.smartsolar.app.api.ApiService;
import com.smartsolar.app.api.models.BookingResponse;
import com.smartsolar.app.auth.SessionManager;
import com.smartsolar.app.db.BookingCacheDao;
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
 * Encodes booking transaction details (ID, NIC, slot, token, timestamp)
 * into a standard QR Code bitmap for operator verification.
 */
public class QrGeneratorActivity extends AppCompatActivity {

    private ImageView ivQrCode;
    private ProgressBar pbQrLoading;
    private TextView tvQrBookingId;
    private TextView tvQrStatusBadge;
    private TextView tvQrNodeName;
    private TextView tvQrDateTime;
    private TextView tvQrEnergy;
    private MaterialButton btnQrDone;

    private SessionManager sessionManager;
    private BookingCacheDao bookingCacheDao;

    private String bookingId;
    private String qrData;
    private BookingResponse currentBooking;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_generator);

        sessionManager = SessionManager.getInstance(this);
        bookingCacheDao = SmartSolarApplication.getInstance().getBookingCacheDao();

        bookingId = getIntent().getStringExtra(Constants.EXTRA_BOOKING_ID);
        qrData = getIntent().getStringExtra(Constants.EXTRA_QR_DATA);

        initViews();
        setupListeners();
        loadBookingAndGenerateQr();
    }

    private void initViews() {
        ivQrCode = findViewById(R.id.ivQrCode);
        pbQrLoading = findViewById(R.id.pbQrLoading);
        tvQrBookingId = findViewById(R.id.tvQrBookingId);
        tvQrStatusBadge = findViewById(R.id.tvQrStatusBadge);
        tvQrNodeName = findViewById(R.id.tvQrNodeName);
        tvQrDateTime = findViewById(R.id.tvQrDateTime);
        tvQrEnergy = findViewById(R.id.tvQrEnergy);
        btnQrDone = findViewById(R.id.btnQrDone);
    }

    private void setupListeners() {
        btnQrDone.setOnClickListener(v -> finish());
    }

    private void loadBookingAndGenerateQr() {
        if (bookingId != null && bookingCacheDao != null) {
            currentBooking = bookingCacheDao.getBookingById(bookingId);
            if (currentBooking != null) {
                bindBookingData(currentBooking);
                generateQrBitmap(buildPayloadString(currentBooking));
            }
        }

        // Fetch latest from API if available
        if (bookingId != null && NetworkUtils.isNetworkAvailable(this)) {
            pbQrLoading.setVisibility(View.VISIBLE);
            ApiService apiService = ApiClient.getApiService(this);
            apiService.getBookingById(bookingId).enqueue(new Callback<BookingResponse>() {
                @Override
                public void onResponse(@NonNull Call<BookingResponse> call,
                                       @NonNull Response<BookingResponse> response) {
                    pbQrLoading.setVisibility(View.GONE);
                    if (response.isSuccessful() && response.body() != null) {
                        currentBooking = response.body();
                        if (bookingCacheDao != null) {
                            bookingCacheDao.insertBooking(currentBooking);
                        }
                        bindBookingData(currentBooking);
                        generateQrBitmap(buildPayloadString(currentBooking));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<BookingResponse> call, @NonNull Throwable t) {
                    pbQrLoading.setVisibility(View.GONE);
                }
            });
        } else if (currentBooking == null) {
            // Fallback: Generate directly from available intent data
            String fallbackPayload = qrData != null ? qrData : ("SMARTSOLAR:BOOKING:" + (bookingId != null ? bookingId : "DEMO"));
            generateQrBitmap(fallbackPayload);
            tvQrBookingId.setText("ID: " + (bookingId != null ? bookingId : "N/A"));
        }
    }

    private String buildPayloadString(BookingResponse booking) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("bookingId", booking.getBookingId() != null ? booking.getBookingId() : bookingId);
        payload.put("prosumerNic", booking.getProsumerNic() != null ? booking.getProsumerNic() : sessionManager.getUserNic());
        payload.put("nodeId", booking.getNodeId() != null ? booking.getNodeId() : "");
        payload.put("energyKwh", booking.getEnergyKwh());
        payload.put("qrToken", booking.getQrToken() != null ? booking.getQrToken() : (qrData != null ? qrData : "TOKEN-" + System.currentTimeMillis()));
        payload.put("timestamp", System.currentTimeMillis());

        return new Gson().toJson(payload);
    }

    private void generateQrBitmap(String content) {
        try {
            pbQrLoading.setVisibility(View.VISIBLE);
            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            BitMatrix bitMatrix = multiFormatWriter.encode(content, BarcodeFormat.QR_CODE, 512, 512);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            ivQrCode.setImageBitmap(bitmap);
            pbQrLoading.setVisibility(View.GONE);
        } catch (Exception e) {
            pbQrLoading.setVisibility(View.GONE);
            Toast.makeText(this, R.string.qr_error_generate, Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void bindBookingData(BookingResponse booking) {
        tvQrBookingId.setText("ID: " + booking.getBookingId());
        tvQrStatusBadge.setText(booking.getStatus() != null ? booking.getStatus() : Constants.STATUS_APPROVED);

        String nodeDisplay = booking.getNodeName() != null ? booking.getNodeName() : "Grid Node (" + booking.getNodeId() + ")";
        tvQrNodeName.setText(nodeDisplay);

        String dateStr = booking.getSlotDate() != null ? booking.getSlotDate() : "";
        Date parsed = DateTimeUtils.parseApiDate(booking.getSlotDate());
        if (parsed != null) {
            dateStr = DateTimeUtils.formatDisplayDate(parsed);
        }
        String timeStr = booking.getSlotTime() != null ? booking.getSlotTime() : "";
        tvQrDateTime.setText(dateStr + " • " + timeStr);

        tvQrEnergy.setText(booking.getEnergyKwh() + " kWh");
    }
}
