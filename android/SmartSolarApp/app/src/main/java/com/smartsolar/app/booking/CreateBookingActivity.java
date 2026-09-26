/*
 * Smart Solar Microgrid Trading System
 * CreateBookingActivity.java
 *
 * Member 2 - Native Android Prosumer Application
 * Handles creating a new energy slot reservation.
 * Enforces the 7-day advance booking window rule and validates node capacity.
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
 * Screen where a prosumer creates a new energy slot booking.
 * - Loads active grid nodes from API / local cache.
 * - Restricts date selection to the next 7 days.
 * - Sends POST /api/bookings and navigates to BookingSummaryActivity on success.
 */
public class CreateBookingActivity extends AppCompatActivity {

    private TextInputLayout tilNode;
    private TextInputLayout tilDate;
    private TextInputLayout tilTimeSlot;
    private TextInputLayout tilEnergyKwh;
    private TextInputLayout tilNotes;

    private AutoCompleteTextView actvNode;
    private TextView tvNodeDetails;
    private TextInputEditText etDate;
    private AutoCompleteTextView actvTimeSlot;
    private TextInputEditText etEnergyKwh;
    private TextInputEditText etNotes;

    private ProgressBar progressBar;
    private MaterialButton btnCreateBooking;
    private MaterialButton btnCancel;

    private SessionManager sessionManager;
    private NodeCacheDao nodeCacheDao;
    private BookingCacheDao bookingCacheDao;

    private List<MicrogridNode> nodeList = new ArrayList<>();
    private MicrogridNode selectedNode;
    private Date selectedDate;
    private Calendar calendar = Calendar.getInstance();

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
        setContentView(R.layout.activity_create_booking);

        sessionManager = SessionManager.getInstance(this);
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        nodeCacheDao = SmartSolarApplication.getInstance().getNodeCacheDao();
        bookingCacheDao = SmartSolarApplication.getInstance().getBookingCacheDao();

        initViews();
        setupTimeSlotDropdown();
        setupDatePicker();
        setupListeners();
        loadNodes();
    }

    private void initViews() {
        tilNode = findViewById(R.id.tilNode);
        tilDate = findViewById(R.id.tilDate);
        tilTimeSlot = findViewById(R.id.tilTimeSlot);
        tilEnergyKwh = findViewById(R.id.tilEnergyKwh);
        tilNotes = findViewById(R.id.tilNotes);

        actvNode = findViewById(R.id.actvNode);
        tvNodeDetails = findViewById(R.id.tvNodeDetails);
        etDate = findViewById(R.id.etDate);
        actvTimeSlot = findViewById(R.id.actvTimeSlot);
        etEnergyKwh = findViewById(R.id.etEnergyKwh);
        etNotes = findViewById(R.id.etNotes);

        progressBar = findViewById(R.id.progressBar);
        btnCreateBooking = findViewById(R.id.btnCreateBooking);
        btnCancel = findViewById(R.id.btnCancel);
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
        etDate.setOnClickListener(v -> showDatePickerDialog());
    }

    private void showDatePickerDialog() {
        Calendar now = Calendar.getInstance();
        int year = now.get(Calendar.YEAR);
        int month = now.get(Calendar.MONTH);
        int day = now.get(Calendar.DAY_OF_MONTH);

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

        // Business Rule: Enforce 7-day window
        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000);
        Calendar maxCalendar = Calendar.getInstance();
        maxCalendar.add(Calendar.DAY_OF_YEAR, Constants.BOOKING_MAX_DAYS_AHEAD);
        datePickerDialog.getDatePicker().setMaxDate(maxCalendar.getTimeInMillis());

        datePickerDialog.show();
    }

    private void setupListeners() {
        btnCreateBooking.setOnClickListener(v -> attemptCreateBooking());
        btnCancel.setOnClickListener(v -> finish());
    }

    /**
     * Loads available grid nodes from local SQLite cache first, then refreshes from API.
     */
    private void loadNodes() {
        if (nodeCacheDao != null) {
            List<MicrogridNode> cachedNodes = nodeCacheDao.getAllNodes();
            if (!cachedNodes.isEmpty()) {
                populateNodeDropdown(cachedNodes);
            }
        }

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
                    // Fallback to cached nodes already handled
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

        // Pre-select if EXTRA_NODE_ID passed in intent
        String preselectedNodeId = getIntent().getStringExtra(Constants.EXTRA_NODE_ID);
        if (preselectedNodeId != null) {
            for (int i = 0; i < nodes.size(); i++) {
                if (nodes.get(i).getNodeId().equalsIgnoreCase(preselectedNodeId)) {
                    selectedNode = nodes.get(i);
                    actvNode.setText(nodeDisplayNames.get(i), false);
                    updateNodeDetailsView(selectedNode);
                    break;
                }
            }
        }

        actvNode.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < nodeList.size()) {
                selectedNode = nodeList.get(position);
                updateNodeDetailsView(selectedNode);
                tilNode.setError(null);
            }
        });
    }

    private void updateNodeDetailsView(MicrogridNode node) {
        if (node != null) {
            String details = "Location: " + node.getLocation() +
                    " • Max Capacity: " + node.getCapacityKwh() + " kWh" +
                    " • Available Slots: " + node.getAvailableSlots();
            tvNodeDetails.setText(details);
            tvNodeDetails.setVisibility(View.VISIBLE);
        } else {
            tvNodeDetails.setVisibility(View.GONE);
        }
    }

    private void attemptCreateBooking() {
        clearErrors();

        boolean cancel = false;
        View focusView = null;

        // Validate node selection
        if (selectedNode == null) {
            tilNode.setError(getString(R.string.booking_error_node_required));
            focusView = actvNode;
            cancel = true;
        }

        // Validate date selection
        if (selectedDate == null) {
            tilDate.setError(getString(R.string.booking_error_date_required));
            if (focusView == null) focusView = etDate;
            cancel = true;
        } else if (!DateTimeUtils.isWithin7DayWindow(selectedDate)) {
            tilDate.setError(getString(R.string.booking_error_7day_rule));
            if (focusView == null) focusView = etDate;
            cancel = true;
        }

        // Validate time slot
        String timeSlot = actvTimeSlot.getText() != null ? actvTimeSlot.getText().toString().trim() : "";
        if (timeSlot.isEmpty()) {
            tilTimeSlot.setError(getString(R.string.booking_error_time_required));
            if (focusView == null) focusView = actvTimeSlot;
            cancel = true;
        }

        // Validate energy kWh
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
                } else if (selectedNode != null && selectedNode.getCapacityKwh() > 0 && energyKwh > selectedNode.getCapacityKwh()) {
                    tilEnergyKwh.setError("Energy cannot exceed node capacity (" + selectedNode.getCapacityKwh() + " kWh)");
                    if (focusView == null) focusView = etEnergyKwh;
                    cancel = true;
                }
            } catch (NumberFormatException e) {
                tilEnergyKwh.setError("Invalid energy number format");
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

        String notes = etNotes.getText() != null ? etNotes.getText().toString().trim() : "";
        executeCreateBooking(selectedNode.getNodeId(), selectedDate, timeSlot, energyKwh, notes);
    }

    private void executeCreateBooking(String nodeId, Date date, String slotTime, double energyKwh, String notes) {
        setLoading(true);

        String userNic = sessionManager.getUserNic();
        String apiDateStr = DateTimeUtils.formatForApi(date);

        BookingRequest request = new BookingRequest(userNic, nodeId, apiDateStr, slotTime, energyKwh);
        request.setNotes(notes);

        ApiService apiService = ApiClient.getApiService(this);
        apiService.createBooking(request).enqueue(new Callback<BookingResponse>() {
            @Override
            public void onResponse(@NonNull Call<BookingResponse> call,
                                   @NonNull Response<BookingResponse> response) {
                setLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    BookingResponse booking = response.body();

                    // If node name missing in response, enrich from selected node
                    if (booking.getNodeName() == null && selectedNode != null) {
                        booking.setNodeName(selectedNode.getNodeName());
                    }
                    if (booking.getProsumerNic() == null) {
                        booking.setProsumerNic(userNic);
                    }

                    // Cache in local SQLite
                    if (bookingCacheDao != null) {
                        bookingCacheDao.insertBooking(booking);
                    }

                    Toast.makeText(CreateBookingActivity.this,
                            R.string.booking_create_success, Toast.LENGTH_SHORT).show();

                    // Navigate to Summary Screen
                    Intent summaryIntent = new Intent(CreateBookingActivity.this, BookingSummaryActivity.class);
                    summaryIntent.putExtra(Constants.EXTRA_BOOKING_ID, booking.getBookingId());
                    summaryIntent.putExtra(Constants.EXTRA_ACTION_TYPE, "CREATE");
                    summaryIntent.putExtra("node_name", booking.getNodeName() != null ? booking.getNodeName() : selectedNode.getNodeName());
                    summaryIntent.putExtra("slot_date", booking.getSlotDate() != null ? booking.getSlotDate() : apiDateStr);
                    summaryIntent.putExtra("slot_time", booking.getSlotTime() != null ? booking.getSlotTime() : slotTime);
                    summaryIntent.putExtra("energy_kwh", booking.getEnergyKwh() > 0 ? booking.getEnergyKwh() : energyKwh);
                    summaryIntent.putExtra("status", booking.getStatus() != null ? booking.getStatus() : Constants.STATUS_PENDING);
                    summaryIntent.putExtra("message", booking.getMessage());
                    summaryIntent.putExtra("qr_token", booking.getQrToken());

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
                showSnackbar(getString(R.string.booking_create_failed) + ": " + t.getMessage());
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
        return getString(R.string.booking_create_failed);
    }

    private void clearErrors() {
        tilNode.setError(null);
        tilDate.setError(null);
        tilTimeSlot.setError(null);
        tilEnergyKwh.setError(null);
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnCreateBooking.setEnabled(!isLoading);
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
