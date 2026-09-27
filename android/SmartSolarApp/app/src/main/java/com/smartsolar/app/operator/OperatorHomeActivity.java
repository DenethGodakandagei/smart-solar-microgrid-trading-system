/*
 * Smart Solar Microgrid Trading System
 * OperatorHomeActivity.java
 *
 * Member 2 - Native Android Prosumer Application
 * Grid operator home screen with quick QR scanner launch,
 * station operational tools, and session logout.
 */
package com.smartsolar.app.operator;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.smartsolar.app.R;
import com.smartsolar.app.SmartSolarApplication;
import com.smartsolar.app.auth.LoginActivity;
import com.smartsolar.app.auth.SessionManager;
import com.smartsolar.app.map.NearbyNodesMapActivity;
import com.smartsolar.app.qr.QrScannerActivity;

/**
 * Main console activity for grid station operators.
 */
public class OperatorHomeActivity extends AppCompatActivity {

    private TextView tvOperatorGreeting;
    private TextView tvOperatorStation;
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
    }

    private void initViews() {
        tvOperatorGreeting = findViewById(R.id.tvOperatorGreeting);
        tvOperatorStation = findViewById(R.id.tvOperatorStation);
        btnOperatorLogout = findViewById(R.id.btnOperatorLogout);
        cardScanQrCta = findViewById(R.id.cardScanQrCta);
        btnScanQr = findViewById(R.id.btnScanQr);
        btnOperatorMap = findViewById(R.id.btnOperatorMap);
        btnOperatorVerifyDirect = findViewById(R.id.btnOperatorVerifyDirect);

        String name = sessionManager.getUserName();
        tvOperatorGreeting.setText("Welcome, " + (name != null && !name.isEmpty() ? name : "Grid Operator"));
    }

    private void setupListeners() {
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

    private void openQrScanner() {
        startActivity(new Intent(this, QrScannerActivity.class));
    }
}
