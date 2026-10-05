/*
 * Smart Solar Microgrid Trading System
 * UpdateBookingActivity.java
 *
 * Member 2 - Native Android Prosumer Application
 * Handles modifying an existing energy slot reservation.
 * Enforces the 12-hour advance notice rule and 7-day booking window.
 */
package com.smartsolar.app.booking;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.Gson;
import com.smartsolar.app.R;
import com.smartsolar.app.SmartSolarApplication;
import com.smartsolar.app.api.ApiClient;
import com.smartsolar.app.api.ApiService;
import com.smartsolar.app.api.models.ApiError;
import com.smartsolar.app.api.models.BookingRequest;
import com.smartsolar.app.api.models.BookingResponse;
import com.smartsolar.app.api.models.MicrogridNode;
import com.smartsolar.app.auth.LoginActivity;
import com.smartsolar.app.auth.SessionManager;
import com.smartsolar.app.db.BookingCacheDao;
import com.smartsolar.app.db.NodeCacheDao;
import com.smartsolar.app.utils.Constants;
import com.smartsolar.app.utils.DateTimeUtils;
import com.smartsolar.app.utils.NetworkUtils;
import com.smartsolar.app.utils.ValidationUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Allows a prosumer to modify an upcoming booking.
 * - Loads existing reservation data from API / cache.
 * - Checks that slot is >= 12 hours away.
 * - Sends PUT /api/bookings/{id} and routes to BookingSummaryActivity.
 */
public class UpdateBookingActivity extends AppCompatActivity {

    private MaterialCardView cardRuleNotice;
    private TextView tvRuleNoticeIcon;
    private TextView tvRuleNoticeText;
    private TextView tvBookingId;
    private TextView tvBookingStatusBadge;
    private TextView tvNodeSummary;

    private TextInputLayout tilNode;
    private TextInputLayout tilDate;
    private TextInputLayout tilTimeSlot;
    private TextInputLayout tilEnergyKwh;
    private TextInputLayout tilNotes;

    private AutoCompleteTextView actvNode;
    private TextInputEditText etDate;
    private AutoCompleteTextView actvTimeSlot;
    private TextInputEditText etEnergyKwh;
    private TextInputEditText etNotes;

    private ProgressBar progressBar;
    private MaterialButton btnUpdateBooking;
    private MaterialButton btnCancel;

    private SessionManager sessionManager;
    private BookingCacheDao bookingCacheDao;
    private NodeCacheDao nodeCacheDao;

    private String bookingId;
    private BookingResponse currentBooking;
    private List<MicrogridNode> nodeList = new ArrayList<>();
    private MicrogridNode selectedNode;
    private Date selectedDate;
    private Calendar calendar = Calendar.getInstance();
    private boolean isNoticeRuleSatisfied = true;

    private static final String[] TIME_SLOTS = new String[]{
            "08:00 AM - 10:00 AM",
            "10:00 AM - 12:00 PM",
            "12:00 PM - 02:00 PM",
            "02:00 PM - 04:00 PM",
            "04:00 PM - 06:00 PM",
            "06:00 PM - 08:00 PM"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_booking);

        sessionManager = SessionManager.getInstance(this);
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        bookingCacheDao = SmartSolarApplication.getInstance().getBookingCacheDao();
        nodeCacheDao = SmartSolarApplication.getInstance().getNodeCacheDao();

        bookingId = getIntent().getStringExtra(Constants.EXTRA_BOOKING_ID);
        if (bookingId == null || bookingId.isEmpty()) {
            Toast.makeText(this, "Invalid booking reference", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupTimeSlotDropdown();
        setupDatePicker();
        setupListeners();
        loadNodesAndBooking();
    }

    private void initViews() {
        cardRuleNotice = findViewById(R.id.cardRuleNotice);
        tvRuleNoticeIcon = findViewById(R.id.tvRuleNoticeIcon);
        tvRuleNoticeText = findViewById(R.id.tvRuleNoticeText);
        tvBookingId = findViewById(R.id.tvBookingId);
        tvBookingStatusBadge = findViewById(R.id.tvBookingStatusBadge);
        tvNodeSummary = findViewById(R.id.tvNodeSummary);

        tilNode = findViewById(R.id.tilNode);
        tilDate = findViewById(R.id.tilDate);
        tilTimeSlot = findViewById(R.id.tilTimeSlot);
        tilEnergyKwh = findViewById(R.id.tilEnergyKwh);
        tilNotes = findViewById(R.id.tilNotes);

        actvNode = findViewById(R.id.actvNode);
        etDate = findViewById(R.id.etDate);
        actvTimeSlot = findViewById(R.id.actvTimeSlot);
        etEnergyKwh = findViewById(R.id.etEnergyKwh);
        etNotes = findViewById(R.id.etNotes);

        progressBar = findViewById(R.id.progressBar);
        btnUpdateBooking = findViewById(R.id.btnUpdateBooking);
        btnCancel = findViewById(R.id.btnCancel);

        tvBookingId.setText("ID: " + bookingId);
    }

    private void setupTimeSlotDropdown() {
        ArrayAdapter<String> timeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                TIME_SLOTS
        );
        actvTimeSlot.setAdapter(timeAdapter);
    }

    private void setupDatePicker() {
        etDate.setOnClickListener(v -> {
            if (!isNoticeRuleSatisfied) {
                showSnackbar(getString(R.string.booking_error_12hour_rule));
                return;
            }
            showDatePickerDialog();
        });
    }

    private void showDatePickerDialog() {
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH);
        int day = now.get(Calendar.DAY_OF_MONTH);

        if (selectedDate != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(selectedDate);
            year = cal.get(Calendar.YEAR);
            month = cal.get(Calendar.MONTH);
            day = cal.get(Calendar.DAY_OF_MONTH);
        }

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    calendar.set(Calendar.YEAR, selectedYear);
                    calendar.set(Calendar.MONTH, selectedMonth);
                    calendar.set(Calendar.DAY_OF_MONTH, selectedDay);
                    calendar.set(Calendar.HOUR_OF_DAY, 0);
                    calendar.set(Calendar.MINUTE, 0);
                    calendar.set(Calendar.SECOND, 0);
                    calendar.set(Calendar.MILLISECOND, 0);

                    selectedDate = calendar.getTime();
                    SimpleDateFormat displayFormat = new SimpleDateFormat(Constants.DATE_FORMAT_DISPLAY, Locale.US);
                    etDate.setText(displayFormat.format(selectedDate));
                    tilDate.setError(null);
                },
                year,
                month,
                day
        );

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        Calendar maxCalendar = Calendar.getInstance();
        maxCalendar.add(Calendar.DAY_OF_YEAR, Constants.BOOKING_MAX_DAYS_AHEAD);
        datePickerDialog.getDatePicker().setMaxDate(maxCalendar.getTimeInMillis());

        datePickerDialog.show();
    }

    private void setupListeners() {
        btnUpdateBooking.setOnClickListener(v -> attemptUpdateBooking());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void loadNodesAndBooking() {
        // Load cached nodes
        if (nodeCacheDao != null) {
            List<MicrogridNode> cachedNodes = nodeCacheDao.getAllNodes();
            if (!cachedNodes.isEmpty()) {
                populateNodeDropdown(cachedNodes);
            }
        }

        // Fetch fresh nodes
        if (NetworkUtils.isNetworkAvailable(this)) {
            ApiService apiService = ApiClient.getApiService(this);
            apiService.getAllNodes().enqueue(new Callback<List<MicrogridNode>>() {
                @Override
                public void onResponse(@NonNull Call<List<MicrogridNode>> call,
                                       @NonNull Response<List<MicrogridNode>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        List<MicrogridNode> nodes = response.body();
                        if (nodeCacheDao != null) {
                            nodeCacheDao.insertNodes(nodes);
                        }
                        populateNodeDropdown(nodes);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<List<MicrogridNode>> call, @NonNull Throwable t) {
                }
            });
        }

        // Load booking from cache first
        if (bookingCacheDao != null) {
            BookingResponse cached = bookingCacheDao.getBookingById(bookingId);
            if (cached != null) {
                currentBooking = cached;
                populateBookingDetails(cached);
            }
        }

        // Fetch fresh booking from API
        if (NetworkUtils.isNetworkAvailable(this)) {
            setLoading(true);
            ApiService apiService = ApiClient.getApiService(this);
            apiService.getBookingById(bookingId).enqueue(new Callback<BookingResponse>() {
                @Override
                public void onResponse(@NonNull Call<BookingResponse> call,
                                       @NonNull Response<BookingResponse> response) {
                    setLoading(false);
                    if (response.isSuccessful() && response.body() != null) {
                        currentBooking = response.body();
                        if (bookingCacheDao != null) {
                            bookingCacheDao.insertBooking(currentBooking);
                        }
                        populateBookingDetails(currentBooking);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<BookingResponse> call, @NonNull Throwable t) {
                    setLoading(false);
                    if (currentBooking == null) {
                        showSnackbar("Failed to load booking details: " + t.getMessage());
                    }
                }
            });
        }
    }

    private void populateNodeDropdown(List<MicrogridNode> nodes) {
        nodeList = nodes;
        List<String> nodeDisplayNames = new ArrayList<>();
        for (MicrogridNode node : nodes) {
            nodeDisplayNames.add(node.getNodeName() + " (" + node.getLocation() + ")");
        }

        ArrayAdapter<String> nodeAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                nodeDisplayNames
        );
        actvNode.setAdapter(nodeAdapter);

        // Pre-select current booking node if loaded
        if (currentBooking != null && currentBooking.getNodeId() != null) {
            for (int i = 0; i < nodes.size(); i++) {
                if (nodes.get(i).getNodeId().equalsIgnoreCase(currentBooking.getNodeId())) {
                    selectedNode = nodes.get(i);
                    actvNode.setText(nodeDisplayNames.get(i), false);
                    break;
                }
            }
        }

        actvNode.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < nodeList.size()) {
                selectedNode = nodeList.get(position);
                tilNode.setError(null);
            }
        });
    }

    private void populateBookingDetails(BookingResponse booking) {
        tvBookingId.setText("ID: " + booking.getBookingId());
        tvBookingStatusBadge.setText(booking.getStatus() != null ? booking.getStatus() : Constants.STATUS_PENDING);
        tvNodeSummary.setText("Node: " + (booking.getNodeName() != null ? booking.getNodeName() : booking.getNodeId()));

        // Check 12-hour business rule
        Date slotDate = DateTimeUtils.parseApiDate(booking.getSlotDate());
        if (slotDate != null) {
            selectedDate = slotDate;
            etDate.setText(DateTimeUtils.formatDisplayDate(slotDate));
            isNoticeRuleSatisfied = DateTimeUtils.hasMinimum12HourNotice(slotDate);
        } else {
            etDate.setText(booking.getSlotDate() != null ? booking.getSlotDate() : "");
            isNoticeRuleSatisfied = true;
        }

        if (!isNoticeRuleSatisfied) {
            // Under 12 hours notice -> warning
            cardRuleNotice.setCardBackgroundColor(getColor(R.color.status_error_light));
            cardRuleNotice.setStrokeColor(getColor(R.color.status_error));
            tvRuleNoticeIcon.setText("⚠️");
            tvRuleNoticeText.setText("Notice rule violation: Less than 12 hours remain before this slot. Modifications may not be accepted.");
            tvRuleNoticeText.setTextColor(getColor(R.color.status_error));
        }

        if (booking.getSlotTime() != null) {
            actvTimeSlot.setText(booking.getSlotTime(), false);
        }

        etEnergyKwh.setText(String.valueOf(booking.getEnergyKwh()));
        if (booking.getNotes() != null) {
            etNotes.setText(booking.getNotes());
        }

        // If nodes already loaded, match node
        if (nodeList != null && !nodeList.isEmpty() && booking.getNodeId() != null) {
            for (MicrogridNode node : nodeList) {
                if (node.getNodeId().equalsIgnoreCase(booking.getNodeId())) {
                    selectedNode = node;
                    actvNode.setText(node.getNodeName() + " (" + node.getLocation() + ")", false);
                    break;
                }
            }
        }
    }

    private void attemptUpdateBooking() {
        clearErrors();

        boolean cancel = false;
        View focusView = null;

        // Check 12-hour rule notice warning
        if (!isNoticeRuleSatisfied) {
            showSnackbar(getString(R.string.booking_error_12hour_rule));
        }

        // Validate node
        if (selectedNode == null && currentBooking != null) {
            // Keep current nodeId if not changed
        } else if (selectedNode == null) {
            tilNode.setError(getString(R.string.booking_error_node_required));
            focusView = actvNode;
            cancel = true;
        }

        // Validate date
        if (selectedDate == null) {
            tilDate.setError(getString(R.string.booking_error_date_required));
            if (focusView == null) focusView = etDate;
            cancel = true;
        } else if (!DateTimeUtils.isWithin7DayWindow(selectedDate)) {
            tilDate.setError(getString(R.string.booking_error_7day_rule));
            if (focusView == null) focusView = etDate;
            cancel = true;
        }

        // Validate time
        String timeSlot = actvTimeSlot.getText() != null ? actvTimeSlot.getText().toString().trim() : "";
        if (timeSlot.isEmpty()) {
            tilTimeSlot.setError(getString(R.string.booking_error_time_required));
            if (focusView == null) focusView = actvTimeSlot;
            cancel = true;
        }

        // Validate energy
        String energyStr = etEnergyKwh.getText() != null ? etEnergyKwh.getText().toString().trim() : "";
        double energyKwh = 0;
        if (!ValidationUtils.isNotEmpty(energyStr)) {
            tilEnergyKwh.setError("Energy amount is required");
            if (focusView == null) focusView = etEnergyKwh;
            cancel = true;
        } else {
            try {
                energyKwh = Double.parseDouble(energyStr);
                if (energyKwh <= 0) {
                    tilEnergyKwh.setError("Energy must be greater than 0 kWh");
                    if (focusView == null) focusView = etEnergyKwh;
                    cancel = true;
                }
            } catch (NumberFormatException e) {
                tilEnergyKwh.setError("Invalid energy number");
                if (focusView == null) focusView = etEnergyKwh;
                cancel = true;
            }
        }

        if (cancel) {
            if (focusView != null) {
                focusView.requestFocus();
            }
            return;
        }

        if (!NetworkUtils.isNetworkAvailable(this)) {
            showSnackbar(getString(R.string.error_no_internet));
            return;
        }

        String targetNodeId = selectedNode != null ? selectedNode.getNodeId() :
                (currentBooking != null ? currentBooking.getNodeId() : "");
        String notes = etNotes.getText() != null ? etNotes.getText().toString().trim() : "";

        executeUpdateBooking(targetNodeId, selectedDate, timeSlot, energyKwh, notes);
    }

    private void executeUpdateBooking(String nodeId, Date date, String slotTime, double energyKwh, String notes) {
        setLoading(true);

        String userNic = sessionManager.getUserNic();
        String apiDateStr = DateTimeUtils.formatForApi(date);

        BookingRequest request = new BookingRequest(userNic, nodeId, apiDateStr, slotTime, energyKwh);
        request.setNotes(notes);

        ApiService apiService = ApiClient.getApiService(this);
        apiService.updateBooking(bookingId, request).enqueue(new Callback<BookingResponse>() {
            @Override
            public void onResponse(@NonNull Call<BookingResponse> call,
                                   @NonNull Response<BookingResponse> response) {
                setLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    BookingResponse updated = response.body();

                    if (updated.getBookingId() == null) {
                        updated.setBookingId(bookingId);
                    }
                    if (updated.getNodeName() == null && selectedNode != null) {
                        updated.setNodeName(selectedNode.getNodeName());
                    }

                    // Update cache
                    if (bookingCacheDao != null) {
                        bookingCacheDao.updateBooking(updated);
                    }

                    Toast.makeText(UpdateBookingActivity.this,
                            R.string.booking_update_success, Toast.LENGTH_SHORT).show();

                    // Navigate to Summary Screen
                    Intent summaryIntent = new Intent(UpdateBookingActivity.this, BookingSummaryActivity.class);
                    summaryIntent.putExtra(Constants.EXTRA_BOOKING_ID, updated.getBookingId());
                    summaryIntent.putExtra(Constants.EXTRA_ACTION_TYPE, "UPDATE");
                    summaryIntent.putExtra("node_name", updated.getNodeName() != null ? updated.getNodeName() :
                            (selectedNode != null ? selectedNode.getNodeName() : ""));
                    summaryIntent.putExtra("slot_date", updated.getSlotDate() != null ? updated.getSlotDate() : apiDateStr);
                    summaryIntent.putExtra("slot_time", updated.getSlotTime() != null ? updated.getSlotTime() : slotTime);
                    summaryIntent.putExtra("energy_kwh", updated.getEnergyKwh() > 0 ? updated.getEnergyKwh() : energyKwh);
                    summaryIntent.putExtra("status", updated.getStatus() != null ? updated.getStatus() : Constants.STATUS_PENDING);
                    summaryIntent.putExtra("message", updated.getMessage());
                    summaryIntent.putExtra("qr_token", updated.getQrToken());

                    startActivity(summaryIntent);
                    finish();
                } else {
                    String errorMessage = parseErrorMessage(response);
                    showSnackbar(errorMessage);
                }
            }

            @Override
            public void onFailure(@NonNull Call<BookingResponse> call, @NonNull Throwable t) {
                setLoading(false);
                showSnackbar(getString(R.string.booking_update_failed) + ": " + t.getMessage());
            }
        });
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
        } catch (Exception ignored) {
        }
        return getString(R.string.booking_update_failed);
    }

    private void clearErrors() {
        tilNode.setError(null);
        tilDate.setError(null);
        tilTimeSlot.setError(null);
        tilEnergyKwh.setError(null);
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnUpdateBooking.setEnabled(!isLoading);
        btnCancel.setEnabled(!isLoading);
        actvNode.setEnabled(!isLoading);
        etDate.setEnabled(!isLoading);
        actvTimeSlot.setEnabled(!isLoading);
        etEnergyKwh.setEnabled(!isLoading);
        etNotes.setEnabled(!isLoading);
    }

    private void showSnackbar(String message) {
        Snackbar.make(findViewById(android.R.id.content), message, Snackbar.LENGTH_LONG).show();
    }
}
