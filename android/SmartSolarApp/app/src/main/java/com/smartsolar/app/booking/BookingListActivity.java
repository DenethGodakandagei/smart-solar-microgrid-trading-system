/*
 * Smart Solar Microgrid Trading System
 * BookingListActivity.java
 *
 * Member 2 - Native Android Prosumer Application
 * Displays active and pending energy slot reservations for the prosumer.
 * Provides status filters, pull-to-refresh, and action triggers (edit, cancel, QR).
 */
package com.smartsolar.app.booking;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.snackbar.Snackbar;
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
 * Lists the logged-in prosumer's upcoming and pending bookings.
 * Allows filtering by status and quick navigation to modify or cancel reservations.
 */
public class BookingListActivity extends AppCompatActivity implements BookingAdapter.OnBookingClickListener {

    private ImageButton btnSearch;
    private ImageButton btnNewBooking;
    private ChipGroup chipGroupFilter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView rvBookings;
    private ProgressBar progressBar;
    private LinearLayout layoutEmptyState;
    private MaterialButton btnEmptyCreate;

    private SessionManager sessionManager;
    private BookingCacheDao bookingCacheDao;
    private BookingAdapter bookingAdapter;

    private List<BookingResponse> allBookings = new ArrayList<>();
    private String currentFilter = "ALL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_list);

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
        loadBookings();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh bookings when returning to this screen
        loadBookings();
    }

    private void initViews() {
        btnSearch = findViewById(R.id.btnSearch);
        btnNewBooking = findViewById(R.id.btnNewBooking);
        chipGroupFilter = findViewById(R.id.chipGroupFilter);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        rvBookings = findViewById(R.id.rvBookings);
        progressBar = findViewById(R.id.progressBar);
        layoutEmptyState = findViewById(R.id.layoutEmptyState);
        btnEmptyCreate = findViewById(R.id.btnEmptyCreate);
    }

    private void setupRecyclerView() {
        bookingAdapter = new BookingAdapter(this, this);
        rvBookings.setLayoutManager(new LinearLayoutManager(this));
        rvBookings.setAdapter(bookingAdapter);
    }

    private void setupListeners() {
        swipeRefreshLayout.setOnRefreshListener(this::fetchBookingsFromApi);

        btnSearch.setOnClickListener(v -> {
            startActivity(new Intent(this, BookingSearchActivity.class));
        });

        btnNewBooking.setOnClickListener(v -> {
            startActivity(new Intent(this, CreateBookingActivity.class));
        });

        btnEmptyCreate.setOnClickListener(v -> {
            startActivity(new Intent(this, CreateBookingActivity.class));
        });

        chipGroupFilter.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.contains(R.id.chipPending)) {
                currentFilter = Constants.STATUS_PENDING;
            } else if (checkedIds.contains(R.id.chipApproved)) {
                currentFilter = Constants.STATUS_APPROVED;
            } else {
                currentFilter = "ALL";
            }
            applyCurrentFilter();
        });
    }

    private void loadBookings() {
        String nic = sessionManager.getUserNic();

        // Load cached bookings first
        if (bookingCacheDao != null) {
            List<BookingResponse> cached = bookingCacheDao.getBookingsByNic(nic);
            if (!cached.isEmpty()) {
                allBookings = cached;
                applyCurrentFilter();
            }
        }

        // Fetch fresh from API
        fetchBookingsFromApi();
    }

    private void fetchBookingsFromApi() {
        if (!NetworkUtils.isNetworkAvailable(this)) {
            swipeRefreshLayout.setRefreshing(false);
            if (allBookings.isEmpty()) {
                showEmptyState(true);
            }
            return;
        }

        if (allBookings.isEmpty()) {
            progressBar.setVisibility(View.VISIBLE);
        }

        String nic = sessionManager.getUserNic();
        ApiService apiService = ApiClient.getApiService(this);
        apiService.getBookingsByNic(nic).enqueue(new Callback<List<BookingResponse>>() {
            @Override
            public void onResponse(@NonNull Call<List<BookingResponse>> call,
                                   @NonNull Response<List<BookingResponse>> response) {
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    allBookings = response.body();

                    // Update SQLite cache
                    if (bookingCacheDao != null) {
                        bookingCacheDao.insertBookings(allBookings);
                    }

                    applyCurrentFilter();
                } else {
                    if (allBookings.isEmpty()) {
                        showEmptyState(true);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<BookingResponse>> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                if (allBookings.isEmpty()) {
                    showEmptyState(true);
                }
            }
        });
    }

    private void applyCurrentFilter() {
        List<BookingResponse> filtered = new ArrayList<>();

        for (BookingResponse b : allBookings) {
            if ("ALL".equalsIgnoreCase(currentFilter)) {
                // Show current & pending (exclude cancelled unless explicitly searched)
                if (!Constants.STATUS_CANCELLED.equalsIgnoreCase(b.getStatus())) {
                    filtered.add(b);
                }
            } else if (currentFilter.equalsIgnoreCase(b.getStatus())) {
                filtered.add(b);
            }
        }

        bookingAdapter.setBookings(filtered);
        showEmptyState(filtered.isEmpty());
    }

    private void showEmptyState(boolean isEmpty) {
        layoutEmptyState.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvBookings.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
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
        Intent intent = new Intent(this, UpdateBookingActivity.class);
        intent.putExtra(Constants.EXTRA_BOOKING_ID, booking.getBookingId());
        startActivity(intent);
    }

    @Override
    public void onCancelClick(BookingResponse booking) {
        Intent intent = new Intent(this, CancelBookingActivity.class);
        intent.putExtra(Constants.EXTRA_BOOKING_ID, booking.getBookingId());
        startActivity(intent);
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
            Toast.makeText(this, "QR Generator: " + booking.getBookingId(), Toast.LENGTH_SHORT).show();
        }
    }
}
