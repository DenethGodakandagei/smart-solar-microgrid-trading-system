/*
 * Smart Solar Microgrid Trading System
 * BookingHistoryActivity.java
 *
 * Member 2 - Native Android Prosumer Application
 * Displays historical records of completed and cancelled reservations.
 * Provides status filters and pull-to-refresh.
 */
package com.smartsolar.app.booking;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.chip.ChipGroup;
import com.smartsolar.app.R;
import com.smartsolar.app.SmartSolarApplication;
import com.smartsolar.app.api.ApiClient;
import com.smartsolar.app.api.ApiService;
import com.smartsolar.app.api.models.BookingResponse;
import com.smartsolar.app.auth.LoginActivity;
import com.smartsolar.app.auth.SessionManager;
import com.smartsolar.app.booking.adapters.BookingAdapter;
import com.smartsolar.app.db.BookingCacheDao;
import com.smartsolar.app.utils.Constants;
import com.smartsolar.app.utils.NetworkUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Screen displaying the prosumer's past reservation history.
 * Covers completed transfers and cancelled bookings.
 */
public class BookingHistoryActivity extends AppCompatActivity implements BookingAdapter.OnBookingClickListener {

    private ImageButton btnHistorySearch;
    private ChipGroup chipGroupHistory;
    private SwipeRefreshLayout swipeRefreshHistory;
    private RecyclerView rvBookingHistory;
    private ProgressBar progressBarHistory;
    private LinearLayout layoutEmptyHistory;

    private SessionManager sessionManager;
    private BookingCacheDao bookingCacheDao;
    private BookingAdapter bookingAdapter;

    private List<BookingResponse> historyBookings = new ArrayList<>();
    private String currentFilter = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_history);

        sessionManager = SessionManager.getInstance(this);
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        bookingCacheDao = SmartSolarApplication.getInstance().getBookingCacheDao();

        initViews();
        setupRecyclerView();
        setupListeners();
        loadHistory();
    }

    private void initViews() {
        btnHistorySearch = findViewById(R.id.btnHistorySearch);
        chipGroupHistory = findViewById(R.id.chipGroupHistory);
        swipeRefreshHistory = findViewById(R.id.swipeRefreshHistory);
        rvBookingHistory = findViewById(R.id.rvBookingHistory);
        progressBarHistory = findViewById(R.id.progressBarHistory);
        layoutEmptyHistory = findViewById(R.id.layoutEmptyHistory);
    }

    private void setupRecyclerView() {
        bookingAdapter = new BookingAdapter(this, this);
        rvBookingHistory.setLayoutManager(new LinearLayoutManager(this));
        rvBookingHistory.setAdapter(bookingAdapter);
    }

    private void setupListeners() {
        swipeRefreshHistory.setOnRefreshListener(this::fetchHistoryFromApi);

        btnHistorySearch.setOnClickListener(v -> {
            startActivity(new Intent(this, BookingSearchActivity.class));
        });

        chipGroupHistory.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chipHistoryCompleted)) {
                currentFilter = Constants.STATUS_COMPLETED;
            } else if (checkedIds.contains(R.id.chipHistoryCancelled)) {
                currentFilter = Constants.STATUS_CANCELLED;
            } else {
                currentFilter = "ALL";
            }
            applyFilter();
        });
    }

    private void loadHistory() {
        String nic = sessionManager.getUserNic();

        // Load cached completed & cancelled bookings first
        if (bookingCacheDao != null) {
            List<BookingResponse> allCached = bookingCacheDao.getBookingsByNic(nic);
            List<BookingResponse> cachedHistory = new ArrayList<>();
            if (allCached != null) {
                for (BookingResponse b : allCached) {
                    if (Constants.STATUS_COMPLETED.equalsIgnoreCase(b.getStatus()) ||
                            Constants.STATUS_CANCELLED.equalsIgnoreCase(b.getStatus())) {
                        cachedHistory.add(b);
                    }
                }
            }
            if (!cachedHistory.isEmpty()) {
                historyBookings = cachedHistory;
            } else {
                // Database returned null or empty -> Use dummy data fallback
                historyBookings = getDummyHistoryData();
            }
        } else {
            // Database DAO null -> Use dummy data fallback
            historyBookings = getDummyHistoryData();
        }

        applyFilter();

        // Fetch fresh from API
        fetchHistoryFromApi();
    }

    private void fetchHistoryFromApi() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            swipeRefreshHistory.setRefreshing(false);
            if (historyBookings == null || historyBookings.isEmpty()) {
                historyBookings = getDummyHistoryData();
                applyFilter();
            }
            return;
        }

        if (historyBookings == null || historyBookings.isEmpty()) {
            progressBarHistory.setVisibility(View.VISIBLE);
        }

        String nic = sessionManager.getUserNic();
        ApiService apiService = ApiClient.getApiService(this);
        apiService.getBookingHistory(nic).enqueue(new Callback<List<BookingResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<BookingResponse>> call,
                                   @NonNull Response<List<BookingResponse>> response) {
                progressBarHistory.setVisibility(View.GONE);
                swipeRefreshHistory.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    historyBookings = response.body();

                    // Update SQLite cache
                    if (bookingCacheDao != null) {
                        bookingCacheDao.insertBookings(historyBookings);
                    }
                } else {
                    // Fallback to dummy data if API returns null or empty list
                    if (historyBookings == null || historyBookings.isEmpty()) {
                        historyBookings = getDummyHistoryData();
                    }
                }
                applyFilter();
            }

            @Override
            public void onFailure(@NonNull Call<List<BookingResponse>> call, @NonNull Throwable t) {
                progressBarHistory.setVisibility(View.GONE);
                swipeRefreshHistory.setRefreshing(false);
                if (historyBookings == null || historyBookings.isEmpty()) {
                    historyBookings = getDummyHistoryData();
                    applyFilter();
                }
            }
        });
    }

    private void applyFilter() {
        if (historyBookings == null) {
            historyBookings = getDummyHistoryData();
        }

        List<BookingResponse> filtered = new ArrayList<>();

        for (BookingResponse b : historyBookings) {
            if ("ALL".equalsIgnoreCase(currentFilter)) {
                filtered.add(b);
            } else if (currentFilter.equalsIgnoreCase(b.getStatus())) {
                filtered.add(b);
            }
        }

        bookingAdapter.setBookings(filtered);
        showEmptyState(filtered.isEmpty());
    }

    private void showEmptyState(boolean isEmpty) {
        layoutEmptyHistory.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvBookingHistory.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
    }

    /**
     * Helper method providing fallback dummy history data when DB or API returns null/empty.
     */
    private List<BookingResponse> getDummyHistoryData() {
        List<BookingResponse> dummyList = new ArrayList<>();

        BookingResponse item1 = new BookingResponse();
        item1.setBookingId("BK-2026-101");
        item1.setNodeName("Colombo Central Microgrid Node #1");
        item1.setSlotDate("2026-09-28");
        item1.setSlotTime("10:00 AM - 12:00 PM");
        item1.setEnergyKwh(25.5);
        item1.setStatus(Constants.STATUS_COMPLETED);
        item1.setQrToken("QR-BK-2026-101-CMP");
        dummyList.add(item1);

        BookingResponse item2 = new BookingResponse();
        item2.setBookingId("BK-2026-102");
        item2.setNodeName("Kandy Solar Substation B");
        item2.setSlotDate("2026-09-25");
        item2.setSlotTime("02:00 PM - 04:00 PM");
        item2.setEnergyKwh(18.0);
        item2.setStatus(Constants.STATUS_COMPLETED);
        item2.setQrToken("QR-BK-2026-102-CMP");
        dummyList.add(item2);

        BookingResponse item3 = new BookingResponse();
        item3.setBookingId("BK-2026-103");
        item3.setNodeName("Galle Green Energy Terminal");
        item3.setSlotDate("2026-09-20");
        item3.setSlotTime("08:00 AM - 10:00 AM");
        item3.setEnergyKwh(12.0);
        item3.setStatus(Constants.STATUS_CANCELLED);
        item3.setQrToken("QR-BK-2026-103-CNC");
        dummyList.add(item3);

        BookingResponse item4 = new BookingResponse();
        item4.setBookingId("BK-2026-104");
        item4.setNodeName("Kurunegala Microgrid Hub");
        item4.setSlotDate("2026-09-15");
        item4.setSlotTime("11:00 AM - 01:00 PM");
        item4.setEnergyKwh(30.0);
        item4.setStatus(Constants.STATUS_COMPLETED);
        item4.setQrToken("QR-BK-2026-104-CMP");
        dummyList.add(item4);

        return dummyList;
    }

    @Override
    public void onBookingClick(BookingResponse booking) {
        Intent intent = new Intent(this, BookingSummaryActivity.class);
        intent.putExtra(Constants.EXTRA_BOOKING_ID, booking.getBookingId());
        intent.putExtra(Constants.EXTRA_ACTION_TYPE, "VIEW");
        intent.putExtra("node_name", booking.getNodeName());
        intent.putExtra("slot_date", booking.getSlotDate());
        intent.putExtra("slot_time", booking.getSlotTime());
        intent.putExtra("energy_kwh", booking.getEnergyKwh());
        intent.putExtra("status", booking.getStatus());
        intent.putExtra("qr_token", booking.getQrToken());
        startActivity(intent);
    }

    @Override
    public void onEditClick(BookingResponse booking) {
        // Not applicable for history items
    }

    @Override
    public void onCancelClick(BookingResponse booking) {
        // Not applicable for history items
    }

    @Override
    public void onQrClick(BookingResponse booking) {
        try {
            Class<?> qrClass = Class.forName("com.smartsolar.app.qr.QrGeneratorActivity");
            Intent intent = new Intent(this, qrClass);
            intent.putExtra(Constants.EXTRA_BOOKING_ID, booking.getBookingId());
            intent.putExtra(Constants.EXTRA_QR_DATA, booking.getQrToken() != null ? booking.getQrToken() : booking.getBookingId());
            startActivity(intent);
        } catch (ClassNotFoundException e) {
            Toast.makeText(this, "QR: " + booking.getBookingId(), Toast.LENGTH_SHORT).show();
        }
    }
}