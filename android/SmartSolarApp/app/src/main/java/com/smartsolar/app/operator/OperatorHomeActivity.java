/*
 * Smart Solar Microgrid Trading System
 * OperatorHomeActivity.java
 *
 * Member 2 - Native Android Prosumer Application
 * Grid operator home dashboard with pending verifications count,
 * quick QR scanner launch, station operational tools, and session logout.
 */
package com.smartsolar.app.operator;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.smartsolar.app.R;
import com.smartsolar.app.SmartSolarApplication;
import com.smartsolar.app.api.ApiClient;
import com.smartsolar.app.api.ApiService;
import com.smartsolar.app.api.models.DashboardStats;
import com.smartsolar.app.auth.LoginActivity;
import com.smartsolar.app.auth.SessionManager;
import com.smartsolar.app.map.NearbyNodesMapActivity;
import com.smartsolar.app.qr.QrScannerActivity;
import com.smartsolar.app.utils.NetworkUtils;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Main console activity for grid station operators.
 */
public class OperatorHomeActivity extends AppCompatActivity {

    private SwipeRefreshLayout swipeRefreshOperator;
    private TextView tvOperatorGreeting;
    private TextView tvOperatorStation;
    private TextView tvPendingCountValue;
    private TextView tvStationStatusValue;
    private MaterialButton btnOperatorLogout;
    private MaterialCardView cardScanQrCta;
    private MaterialButton btnScanQr;
    private MaterialCardView btnOperatorMap;
    private MaterialCardView btnOperatorVerifyDirect;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_operator_home);

        sessionManager = SessionManager.getInstance(this);
        if (!sessionManager.isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initViews();
        setupListeners();
        loadOperatorStats();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadOperatorStats();
    }

    private void initViews() {
        swipeRefreshOperator = findViewById(R.id.swipeRefreshOperator);
        tvOperatorGreeting = findViewById(R.id.tvOperatorGreeting);
        tvOperatorStation = findViewById(R.id.tvOperatorStation);
        tvPendingCountValue = findViewById(R.id.tvPendingCountValue);
        tvStationStatusValue = findViewById(R.id.tvStationStatusValue);
        btnOperatorLogout = findViewById(R.id.btnOperatorLogout);
        cardScanQrCta = findViewById(R.id.cardScanQrCta);
        btnScanQr = findViewById(R.id.btnScanQr);
        btnOperatorMap = findViewById(R.id.btnOperatorMap);
        btnOperatorVerifyDirect = findViewById(R.id.btnOperatorVerifyDirect);

        String name = sessionManager.getUserName();
        tvOperatorGreeting.setText("Welcome, " + (name != null && !name.isEmpty() ? name : "Grid Operator"));
    }

    private void setupListeners() {
        if (swipeRefreshOperator != null) {
            swipeRefreshOperator.setOnRefreshListener(this::loadOperatorStats);
        }

        cardScanQrCta.setOnClickListener(v -> openQrScanner());
        btnScanQr.setOnClickListener(v -> openQrScanner());

        btnOperatorMap.setOnClickListener(v -> {
            startActivity(new Intent(this, NearbyNodesMapActivity.class));
        });

        btnOperatorVerifyDirect.setOnClickListener(v -> {
            startActivity(new Intent(this, VerifyTransactionActivity.class));
        });

        btnOperatorLogout.setOnClickListener(v -> {
            SmartSolarApplication.getInstance().performLogout();
            Intent logoutIntent = new Intent(this, LoginActivity.class);
            logoutIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(logoutIntent);
            finish();
        });
    }

    private void loadOperatorStats() {
        if (swipeRefreshOperator != null) {
            swipeRefreshOperator.setRefreshing(true);
        }

        if (NetworkUtils.isNetworkAvailable(this)) {
            ApiService apiService = ApiClient.getApiService(this);
            String nic = sessionManager.getUserNic();
            apiService.getDashboardStats(nic).enqueue(new Callback<DashboardStats>() {
                @Override
                public void onResponse(@NonNull Call<DashboardStats> call, @NonNull Response<DashboardStats> response) {
                    if (swipeRefreshOperator != null) {
                        swipeRefreshOperator.setRefreshing(false);
                    }
                    if (response.isSuccessful() && response.body() != null) {
                        tvPendingCountValue.setText(String.valueOf(response.body().getPendingCount()));
                    } else {
                        tvPendingCountValue.setText("0");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<DashboardStats> call, @NonNull Throwable t) {
                    if (swipeRefreshOperator != null) {
                        swipeRefreshOperator.setRefreshing(false);
                    }
                    tvPendingCountValue.setText("0");
                }
            });
        } else {
            if (swipeRefreshOperator != null) {
                swipeRefreshOperator.setRefreshing(false);
            }
            tvPendingCountValue.setText("0");
            Toast.makeText(this, R.string.error_no_internet, Toast.LENGTH_SHORT).show();
        }
    }

    private void openQrScanner() {
        startActivity(new Intent(this, QrScannerActivity.class));
    }
}
