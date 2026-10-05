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
            for (BookingResponse b : allCached) {
                if (Constants.STATUS_COMPLETED.equalsIgnoreCase(b.getStatus()) ||
                        Constants.STATUS_CANCELLED.equalsIgnoreCase(b.getStatus())) {
                    cachedHistory.add(b);
                }
            }
            if (!cachedHistory.isEmpty()) {
                historyBookings = cachedHistory;
                applyFilter();
            }
        }

        // Fetch fresh from API
        fetchHistoryFromApi();
    }

    private void fetchHistoryFromApi() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            swipeRefreshHistory.setRefreshing(false);
            if (historyBookings.isEmpty()) {
                showEmptyState(true);
            }
            return;
        }

        if (historyBookings.isEmpty()) {
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

                if (response.isSuccessful() && response.body() != null) {
                    historyBookings = response.body();

                    // Update SQLite cache
                    if (bookingCacheDao != null) {
                        bookingCacheDao.insertBookings(historyBookings);
                    }

                    applyFilter();
                } else {
                    if (historyBookings.isEmpty()) {
                        showEmptyState(true);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<BookingResponse>> call, @NonNull Throwable t) {
                progressBarHistory.setVisibility(View.GONE);
                swipeRefreshHistory.setRefreshing(false);
                if (historyBookings.isEmpty()) {
                    showEmptyState(true);
                }
            }
        });
    }

    private void applyFilter() {
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
