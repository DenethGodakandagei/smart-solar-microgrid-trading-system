/*
 * Smart Solar Microgrid Trading System
 * DashboardActivity.java
 *
 * Member 2 - Native Android Prosumer Application
 * Prosumer home dashboard activity displaying live statistics,
 * quick actions, recent reservations, and bottom navigation.
 */
package com.smartsolar.app.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.card.MaterialCardView;
import com.smartsolar.app.R;
import com.smartsolar.app.SmartSolarApplication;
import com.smartsolar.app.account.ProfileActivity;
import com.smartsolar.app.api.ApiClient;
import com.smartsolar.app.api.ApiService;
import com.smartsolar.app.api.models.BookingResponse;
import com.smartsolar.app.api.models.DashboardStats;
import com.smartsolar.app.auth.LoginActivity;
import com.smartsolar.app.auth.SessionManager;
import com.smartsolar.app.booking.BookingHistoryActivity;
import com.smartsolar.app.booking.BookingListActivity;
import com.smartsolar.app.booking.BookingSummaryActivity;
import com.smartsolar.app.booking.CancelBookingActivity;
import com.smartsolar.app.booking.CreateBookingActivity;
import com.smartsolar.app.booking.UpdateBookingActivity;
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
 * Main prosumer home screen after login.
 * Displays energy statistics (pending & approved bookings), quick action shortcuts,
 * recent booking list, and bottom navigation.
 */
public class DashboardActivity extends AppCompatActivity implements BookingAdapter.OnBookingClickListener {

    private SwipeRefreshLayout swipeRefreshDashboard;
    private TextView tvGreeting;
    private TextView tvUserNic;
    private ImageButton btnHeaderProfile;

    private MaterialCardView cardPendingStats;
    private MaterialCardView cardApprovedStats;
    private TextView tvPendingCount;
    private TextView tvApprovedCount;

    private MaterialCardView btnQuickNewBooking;
    private MaterialCardView btnQuickMyBookings;
    private MaterialCardView btnQuickHistory;
    private MaterialCardView btnQuickNearbyNodes;

    private TextView tvViewAllBookings;
    private RecyclerView rvRecentBookings;
    private LinearLayout layoutRecentEmpty;
    private BottomNavigationView bottomNavigation;

    private SessionManager sessionManager;
    private BookingCacheDao bookingCacheDao;
    private BookingAdapter recentBookingsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

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
        setupBottomNavigation();
        loadDashboardData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        bottomNavigation.setSelectedItemId(R.id.nav_dashboard);
        loadDashboardData();
    }

    private void initViews() {
        swipeRefreshDashboard = findViewById(R.id.swipeRefreshDashboard);
        tvGreeting = findViewById(R.id.tvGreeting);
        tvUserNic = findViewById(R.id.tvUserNic);
        btnHeaderProfile = findViewById(R.id.btnHeaderProfile);

        cardPendingStats = findViewById(R.id.cardPendingStats);
        cardApprovedStats = findViewById(R.id.cardApprovedStats);
        tvPendingCount = findViewById(R.id.tvPendingCount);
        tvApprovedCount = findViewById(R.id.tvApprovedCount);

        btnQuickNewBooking = findViewById(R.id.btnQuickNewBooking);
        btnQuickMyBookings = findViewById(R.id.btnQuickMyBookings);
        btnQuickHistory = findViewById(R.id.btnQuickHistory);
        btnQuickNearbyNodes = findViewById(R.id.btnQuickNearbyNodes);

        tvViewAllBookings = findViewById(R.id.tvViewAllBookings);
        rvRecentBookings = findViewById(R.id.rvRecentBookings);
        layoutRecentEmpty = findViewById(R.id.layoutRecentEmpty);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Display user details in greeting
        String userName = sessionManager.getUserName();
        String userNic = sessionManager.getUserNic();
        tvGreeting.setText(getString(R.string.dashboard_greeting, userName != null && !userName.isEmpty() ? userName : "Prosumer"));
        tvUserNic.setText("NIC: " + userNic + " • Solar Prosumer");
    }

    private void setupRecyclerView() {
        recentBookingsAdapter = new BookingAdapter(this, this);
        rvRecentBookings.setLayoutManager(new LinearLayoutManager(this));
        rvRecentBookings.setAdapter(recentBookingsAdapter);
    }

    private void setupListeners() {
        swipeRefreshDashboard.setOnRefreshListener(this::loadDashboardData);

        btnHeaderProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });

        cardPendingStats.setOnClickListener(v -> {
            startActivity(new Intent(this, BookingListActivity.class));
        });

        cardApprovedStats.setOnClickListener(v -> {
            startActivity(new Intent(this, BookingListActivity.class));
        });

        btnQuickNewBooking.setOnClickListener(v -> {
            startActivity(new Intent(this, CreateBookingActivity.class));
        });

        btnQuickMyBookings.setOnClickListener(v -> {
            startActivity(new Intent(this, BookingListActivity.class));
        });

        btnQuickHistory.setOnClickListener(v -> {
            startActivity(new Intent(this, BookingHistoryActivity.class));
        });

        btnQuickNearbyNodes.setOnClickListener(v -> {
            try {
                Class<?> mapClass = Class.forName("com.smartsolar.app.map.NearbyNodesMapActivity");
                startActivity(new Intent(this, mapClass));
            } catch (ClassNotFoundException e) {
                Toast.makeText(this, "Map module will be available in Part 10", Toast.LENGTH_SHORT).show();
            }
        });

        tvViewAllBookings.setOnClickListener(v -> {
            startActivity(new Intent(this, BookingListActivity.class));
        });
    }

    private void setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_dashboard) {
                return true;
            } else if (itemId == R.id.nav_bookings) {
                startActivity(new Intent(this, BookingListActivity.class));
                return true;
            } else if (itemId == R.id.nav_map) {
                try {
                    Class<?> mapClass = Class.forName("com.smartsolar.app.map.NearbyNodesMapActivity");
                    startActivity(new Intent(this, mapClass));
                } catch (ClassNotFoundException e) {
                    Toast.makeText(this, "Map module will be available in Part 10", Toast.LENGTH_SHORT).show();
                }
                return true;
            } else if (itemId == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    private void loadDashboardData() {
        String nic = sessionManager.getUserNic();

        // 1. Load local cache stats & recent items first for immediate response
        if (bookingCacheDao != null) {
            List<BookingResponse> cached = bookingCacheDao.getBookingsByNic(nic);
            int pending = 0;
            int approved = 0;
            for (BookingResponse b : cached) {
                if (Constants.STATUS_PENDING.equalsIgnoreCase(b.getStatus())) pending++;
                if (Constants.STATUS_APPROVED.equalsIgnoreCase(b.getStatus())) approved++;
            }
            tvPendingCount.setText(String.valueOf(pending));
            tvApprovedCount.setText(String.valueOf(approved));

            updateRecentBookingsList(cached);
        }

        // 2. Fetch fresh stats from API
        if (NetworkUtils.isNetworkAvailable(this)) {
            ApiService apiService = ApiClient.getApiService(this);

            // Fetch dashboard counts
            apiService.getDashboardStats(nic).enqueue(new Callback<DashboardStats>() {
                @Override
                public void onResponse(@NonNull Call<DashboardStats> call,
                                       @NonNull Response<DashboardStats> response) {
                    swipeRefreshDashboard.setRefreshing(false);
                    if (response.isSuccessful() && response.body() != null) {
                        DashboardStats stats = response.body();
                        tvPendingCount.setText(String.valueOf(stats.getPendingCount()));
                        tvApprovedCount.setText(String.valueOf(stats.getApprovedCount()));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<DashboardStats> call, @NonNull Throwable t) {
                    swipeRefreshDashboard.setRefreshing(false);
                }
            });

            // Fetch recent bookings
            apiService.getBookingsByNic(nic).enqueue(new Callback<List<BookingResponse>>() {
                @Override
                public void onResponse(@NonNull Call<List<BookingResponse>> call,
                                       @NonNull Response<List<BookingResponse>> response) {
                    swipeRefreshDashboard.setRefreshing(false);
                    if (response.isSuccessful() && response.body() != null) {
                        List<BookingResponse> bookings = response.body();
                        if (bookingCacheDao != null) {
                            bookingCacheDao.insertBookings(bookings);
                        }
                        updateRecentBookingsList(bookings);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<List<BookingResponse>> call, @NonNull Throwable t) {
                    swipeRefreshDashboard.setRefreshing(false);
                }
            });
        } else {
            swipeRefreshDashboard.setRefreshing(false);
        }
    }

    private void updateRecentBookingsList(List<BookingResponse> bookings) {
        if (bookings == null || bookings.isEmpty()) {
            layoutRecentEmpty.setVisibility(View.VISIBLE);
            rvRecentBookings.setVisibility(View.GONE);
            return;
        }

        // Display up to 3 most recent bookings
        List<BookingResponse> recent = new ArrayList<>();
        int limit = Math.min(bookings.size(), 3);
        for (int i = 0; i < limit; i++) {
            recent.add(bookings.get(i));
        }

        recentBookingsAdapter.setBookings(recent);
        layoutRecentEmpty.setVisibility(View.GONE);
        rvRecentBookings.setVisibility(View.VISIBLE);
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
