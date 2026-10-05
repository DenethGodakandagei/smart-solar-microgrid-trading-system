/*
 * Smart Solar Microgrid Trading System
 * QrScannerActivity.java
 *
 * Member 2 - Native Android Prosumer Application
 * Camera viewfinder activity using ZXing Embedded to scan and decode
 * prosumer transaction QR codes for grid operators.
 */
package com.smartsolar.app.qr;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.zxing.ResultPoint;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;
import com.smartsolar.app.R;
import com.smartsolar.app.operator.VerifyTransactionActivity;
import com.smartsolar.app.utils.Constants;

import java.util.List;

/**
 * Camera QR scanner for grid operators.
 * Scans prosumer transaction QR tokens and navigates to VerifyTransactionActivity.
 */
public class QrScannerActivity extends AppCompatActivity {

    private DecoratedBarcodeView barcodeScannerView;
    private ImageButton btnScannerBack;
    private ImageButton btnToggleFlash;
    private MaterialButton btnManualEntry;
    private ProgressBar pbScanning;

    private boolean isFlashOn = false;
    private boolean isProcessingScan = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_scanner);

        initViews();
        setupListeners();
        checkCameraPermission();
    }

    private void initViews() {
        barcodeScannerView = findViewById(R.id.barcodeScannerView);
        btnScannerBack = findViewById(R.id.btnScannerBack);
        btnToggleFlash = findViewById(R.id.btnToggleFlash);
        btnManualEntry = findViewById(R.id.btnManualEntry);
        pbScanning = findViewById(R.id.pbScanning);
    }

    private void setupListeners() {
        btnScannerBack.setOnClickListener(v -> finish());

        btnToggleFlash.setOnClickListener(v -> toggleFlashlight());

        btnManualEntry.setOnClickListener(v -> showManualEntryDialog());
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    Constants.REQUEST_CAMERA_PERMISSION
            );
        } else {
            startScanning();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startScanning();
            } else {
                Toast.makeText(this, R.string.map_error_permission, Toast.LENGTH_LONG).show();
                // If permission denied, open manual entry dialog
                showManualEntryDialog();
            }
        }
    }

    private void startScanning() {
        barcodeScannerView.decodeSingle(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result != null && result.getText() != null && !isProcessingScan) {
                    isProcessingScan = true;
                    pbScanning.setVisibility(View.VISIBLE);
                    handleScannedData(result.getText());
                }
            }

            @Override
            public void possibleResultPoints(List<ResultPoint> resultPoints) {}
        });
    }

    private void handleScannedData(String qrRawText) {
        Intent verifyIntent = new Intent(this, VerifyTransactionActivity.class);
        verifyIntent.putExtra(Constants.EXTRA_QR_DATA, qrRawText);
        startActivity(verifyIntent);
        finish();
    }

    private void toggleFlashlight() {
        if (isFlashOn) {
            barcodeScannerView.setTorchOff();
            isFlashOn = false;
        } else {
            barcodeScannerView.setTorchOn();
            isFlashOn = true;
        }
    }

    private void showManualEntryDialog() {
        EditText input = new EditText(this);
        input.setHint("Enter Booking ID or QR Token");
        input.setPadding(40, 30, 40, 30);

        new MaterialAlertDialogBuilder(this)
                .setTitle("Manual Verification Entry")
                .setMessage("Enter the prosumer booking reference or scan token:")
                .setView(input)
                .setPositiveButton("Verify", (dialog, which) -> {
                    String manualText = input.getText().toString().trim();
                    if (!manualText.isEmpty()) {
                        handleScannedData(manualText);
                    } else {
                        Toast.makeText(QrScannerActivity.this, "Input cannot be empty", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        barcodeScannerView.resume();
        isProcessingScan = false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeScannerView.pause();
    }
}
