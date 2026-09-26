/*
 * Smart Solar Microgrid Trading System
 * BookingSearchActivity.java
 *
 * Member 2 - Native Android Prosumer Application
 * Handles searching and multi-criteria filtering of energy bookings
 * by query text, status, node, and date.
 */
package com.smartsolar.app.booking;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.smartsolar.app.R;
import com.smartsolar.app.SmartSolarApplication;
import com.smartsolar.app.api.ApiClient;
import com.smartsolar.app.api.ApiService;
import com.smartsolar.app.api.models.BookingResponse;
import com.smartsolar.app.api.models.MicrogridNode;
import com.smartsolar.app.auth.LoginActivity;
import com.smartsolar.app.auth.SessionManager;
import com.smartsolar.app.booking.adapters.BookingAdapter;
import com.smartsolar.app.db.BookingCacheDao;
import com.smartsolar.app.db.NodeCacheDao;
import com.smartsolar.app.utils.Constants;
import com.smartsolar.app.utils.DateTimeUtils;
import com.smartsolar.app.utils.NetworkUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Screen providing comprehensive search and filtering across all prosumer reservations.
 */
public class BookingSearchActivity extends AppCompatActivity implements BookingAdapter.OnBookingClickListener {

    private TextInputEditText etSearchQuery;
    private AutoCompleteTextView actvSearchStatus;
    private AutoCompleteTextView actvSearchNode;
    private MaterialButton btnFilterDate;
    private MaterialButton btnClearFilters;
    private TextView tvSearchResultsCount;
    private RecyclerView rvSearchResults;
    private ProgressBar progressBarSearch;
    private LinearLayout layoutSearchEmpty;

    private SessionManager sessionManager;
    private BookingCacheDao bookingCacheDao;
    private NodeCacheDao nodeCacheDao;
    private BookingAdapter bookingAdapter;

    private List<BookingResponse> allBookings = new ArrayList<>();
    private List<MicrogridNode> allNodes = new ArrayList<>();

    private String selectedStatus = "ALL";
    private String selectedNodeId = "ALL";
    private Date selectedDate = null;
    private String currentQuery = "";

    private static final String[] STATUS_OPTIONS = new String[]{
            "All Statuses",
            Constants.STATUS_PENDING,
            Constants.STATUS_APPROVED,
            Constants.STATUS_COMPLETED,
            Constants.STATUS_CANCELLED
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_booking_search);

        sessionManager = SessionManager.getInstance(this);
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        bookingCacheDao = SmartSolarApplication.getInstance().getBookingCacheDao();
        nodeCacheDao = SmartSolarApplication.getInstance().getNodeCacheDao();

        initViews();
        setupRecyclerView();
        setupDropdowns();
        setupListeners();
        loadInitialData();
    }

    private void initViews() {
        etSearchQuery = findViewById(R.id.etSearchQuery);
        actvSearchStatus = findViewById(R.id.actvSearchStatus);
        actvSearchNode = findViewById(R.id.actvSearchNode);
        btnFilterDate = findViewById(R.id.btnFilterDate);
        btnClearFilters = findViewById(R.id.btnClearFilters);
        tvSearchResultsCount = findViewById(R.id.tvSearchResultsCount);
        rvSearchResults = findViewById(R.id.rvSearchResults);
        progressBarSearch = findViewById(R.id.progressBarSearch);
        layoutSearchEmpty = findViewById(R.id.layoutSearchEmpty);
    }

    private void setupRecyclerView() {
        bookingAdapter = new BookingAdapter(this, this);
        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));
        rvSearchResults.setAdapter(bookingAdapter);
    }

    private void setupDropdowns() {
        // Status Dropdown
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                STATUS_OPTIONS
        );
        actvSearchStatus.setAdapter(statusAdapter);
        actvSearchStatus.setText(STATUS_OPTIONS[0], false);

        actvSearchStatus.setOnItemClickListener((parent, view, position, id) -> {
            if (position == 0) {
                selectedStatus = "ALL";
            } else {
                selectedStatus = STATUS_OPTIONS[position];
            }
            performSearch();
        });
    }

    private void setupListeners() {
        etSearchQuery.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentQuery = s != null ? s.toString().trim() : "";
                performSearch();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        btnFilterDate.setOnClickListener(v -> showDatePickerDialog());

        btnClearFilters.setOnClickListener(v -> resetFilters());
    }

    private void showDatePickerDialog() {
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH);
        int day = now.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    Calendar cal = Calendar.getInstance();
                    cal.set(selectedYear, selectedMonth, selectedDay, 0, 0, 0);
                    selectedDate = cal.getTime();

                    SimpleDateFormat sdf = new SimpleDateFormat(Constants.DATE_FORMAT_DISPLAY, Locale.US);
                    btnFilterDate.setText(sdf.format(selectedDate));
                    performSearch();
                },
                year,
                month,
                day
        );
        datePickerDialog.show();
    }

    private void resetFilters() {
        etSearchQuery.setText("");
        currentQuery = "";
        selectedStatus = "ALL";
        selectedNodeId = "ALL";
        selectedDate = null;

        actvSearchStatus.setText(STATUS_OPTIONS[0], false);
        if (!allNodes.isEmpty()) {
            actvSearchNode.setText("All Nodes", false);
        }
        btnFilterDate.setText(R.string.filter_by_date);

        performSearch();
    }

    private void loadInitialData() {
        String nic = sessionManager.getUserNic();

        // Load cached nodes
        if (nodeCacheDao != null) {
            allNodes = nodeCacheDao.getAllNodes();
            populateNodeDropdown(allNodes);
        }

        // Load cached bookings
        if (bookingCacheDao != null) {
            allBookings = bookingCacheDao.getBookingsByNic(nic);
            performSearch();
        }

        // Fetch fresh nodes and bookings from API
        if (NetworkUtils.isNetworkAvailable(this)) {
            ApiService apiService = ApiClient.getApiService(this);

            // Fetch nodes
            apiService.getAllNodes().enqueue(new Callback<List<MicrogridNode>>() {
                @Override
                public void onResponse(@NonNull Call<List<MicrogridNode>> call,
                                       @NonNull Response<List<MicrogridNode>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        allNodes = response.body();
                        populateNodeDropdown(allNodes);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<List<MicrogridNode>> call, @NonNull Throwable t) {}
            });

            // Fetch bookings
            progressBarSearch.setVisibility(View.VISIBLE);
            apiService.getBookingsByNic(nic).enqueue(new Callback<List<BookingResponse>>() {
                @Override
                public void onResponse(@NonNull Call<List<BookingResponse>> call,
                                       @NonNull Response<List<BookingResponse>> response) {
                    progressBarSearch.setVisibility(View.GONE);
                    if (response.isSuccessful() && response.body() != null) {
                        allBookings = response.body();
                        if (bookingCacheDao != null) {
                            bookingCacheDao.insertBookings(allBookings);
                        }
                        performSearch();
                    }
                }

                @Override
                public void onFailure(@NonNull Call<List<BookingResponse>> call, @NonNull Throwable t) {
                    progressBarSearch.setVisibility(View.GONE);
                }
            });
        }
    }

    private void populateNodeDropdown(List<MicrogridNode> nodes) {
        List<String> nodeNames = new ArrayList<>();
        nodeNames.add("All Nodes");
        for (MicrogridNode n : nodes) {
            nodeNames.add(n.getNodeName());
        }

        ArrayAdapter<String> nodeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                nodeNames
        );
        actvSearchNode.setAdapter(nodeAdapter);
        actvSearchNode.setText("All Nodes", false);

        actvSearchNode.setOnItemClickListener((parent, view, position, id) -> {
            if (position == 0) {
                selectedNodeId = "ALL";
            } else if (position - 1 < allNodes.size()) {
                selectedNodeId = allNodes.get(position - 1).getNodeId();
            }
            performSearch();
        });
    }

    /**
     * Filters the bookings list based on query, status, node, and date.
     */
    private void performSearch() {
        List<BookingResponse> results = new ArrayList<>();
        String queryLower = currentQuery.toLowerCase();

        SimpleDateFormat compareFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String selectedDateString = selectedDate != null ? compareFormat.format(selectedDate) : null;

        for (BookingResponse b : allBookings) {
            // 1. Text Query Match (Booking ID, Node Name, Notes)
            boolean matchesQuery = true;
            if (!queryLower.isEmpty()) {
                boolean idMatch = b.getBookingId() != null && b.getBookingId().toLowerCase().contains(queryLower);
                boolean nodeMatch = b.getNodeName() != null && b.getNodeName().toLowerCase().contains(queryLower);
                boolean notesMatch = b.getNotes() != null && b.getNotes().toLowerCase().contains(queryLower);
                matchesQuery = idMatch || nodeMatch || notesMatch;
            }

            // 2. Status Match
            boolean matchesStatus = true;
            if (!"ALL".equalsIgnoreCase(selectedStatus)) {
                matchesStatus = selectedStatus.equalsIgnoreCase(b.getStatus());
            }

            // 3. Node Match
            boolean matchesNode = true;
            if (!"ALL".equalsIgnoreCase(selectedNodeId)) {
                matchesNode = selectedNodeId.equalsIgnoreCase(b.getNodeId());
            }

            // 4. Date Match
            boolean matchesDate = true;
            if (selectedDateString != null && b.getSlotDate() != null) {
                Date itemDate = DateTimeUtils.parseApiDate(b.getSlotDate());
                if (itemDate != null) {
                    matchesDate = selectedDateString.equals(compareFormat.format(itemDate));
                }
            }

            if (matchesQuery && matchesStatus && matchesNode && matchesDate) {
                results.add(b);
            }
        }

        bookingAdapter.setBookings(results);
        tvSearchResultsCount.setText("Found " + results.size() + " booking" + (results.size() == 1 ? "" : "s"));

        boolean isEmpty = results.isEmpty();
        layoutSearchEmpty.setVisibility(isEmpty ? View.VISIBLE : View.GONE);
        rvSearchResults.setVisibility(isEmpty ? View.GONE : View.VISIBLE);
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
            Toast.makeText(this, "QR: " + booking.getBookingId(), Toast.LENGTH_SHORT).show();
        }
    }
}
