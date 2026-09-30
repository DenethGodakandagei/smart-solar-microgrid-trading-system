 /*
         * Smart Solar Microgrid Trading System
         * QrScannerActivity.java
         *
         * Member 4 - Grid Operator App + Integration
         *
         * Purpose:
         * Provides a native Android QR scanner for Grid Operators.
         *
         * Responsibilities:
         * - Request camera permission.
         * - Scan prosumer transaction QR codes.
         * - Support flashlight control.
         * - Provide manual transaction entry when scanning is unavailable.
         * - Pass the scanned transaction data to the verification screen.
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
 * QR scanner activity used by Grid Operators.
 *
 * The scanned QR content is passed to VerifyTransactionActivity,
 * where the transaction is verified against the central Web API.
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

    /**
     * Connect the Java variables with the QR scanner
     * views defined in activity_qr_scanner.xml.
     */
    private void initViews() {

        barcodeScannerView =
                findViewById(R.id.barcodeScannerView);

        btnScannerBack =
                findViewById(R.id.btnScannerBack);

        btnToggleFlash =
                findViewById(R.id.btnToggleFlash);

        btnManualEntry =
                findViewById(R.id.btnManualEntry);

        pbScanning =
                findViewById(R.id.pbScanning);
    }

    /**
     * Configure QR scanner controls.
     */
    private void setupListeners() {

        /*
         * Close the scanner and return to the previous screen.
         */
        btnScannerBack.setOnClickListener(
                v -> finish()
        );

        /*
         * Turn the camera flashlight on or off.
         */
        btnToggleFlash.setOnClickListener(
                v -> toggleFlashlight()
        );

        /*
         * Allow the operator to enter a booking ID
         * or QR token manually.
         */
        btnManualEntry.setOnClickListener(
                v -> showManualEntryDialog()
        );
    }

    /**
     * Check whether the application has permission to
     * use the device camera.
     */
    private void checkCameraPermission() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
        ) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.CAMERA},
                    Constants.REQUEST_CAMERA_PERMISSION
            );

        } else {

            startScanning();
        }
    }

    /**
     * Handle the result of the camera permission request.
     */
    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {

        super.onRequestPermissionsResult(
                requestCode,
                permissions,
                grantResults
        );

        if (requestCode ==
                Constants.REQUEST_CAMERA_PERMISSION) {

            if (grantResults.length > 0
                    && grantResults[0]
                    == PackageManager.PERMISSION_GRANTED) {

                startScanning();

            } else {

                Toast.makeText(
                        this,
                        R.string.map_error_permission,
                        Toast.LENGTH_LONG
                ).show();

                /*
                 * Manual verification is provided as a fallback
                 * when camera permission is unavailable.
                 */
                showManualEntryDialog();
            }
        }
    }

    /**
     * Start listening for a single QR/barcode result.
     */
    private void startScanning() {

        if (barcodeScannerView == null) {
            return;
        }

        isProcessingScan = false;

        barcodeScannerView.decodeSingle(
                new BarcodeCallback() {

                    /**
                     * Handle a successfully decoded QR code.
                     */
                    @Override
                    public void barcodeResult(
                            BarcodeResult result
                    ) {

                        if (result == null) {
                            return;
                        }

                        String scannedText =
                                result.getText();

                        if (scannedText == null
                                || scannedText.trim().isEmpty()) {
                            return;
                        }

                        /*
                         * Prevent the same QR code from opening
                         * the verification screen multiple times.
                         */
                        if (isProcessingScan) {
                            return;
                        }

                        isProcessingScan = true;

                        if (pbScanning != null) {
                            pbScanning.setVisibility(
                                    View.VISIBLE
                            );
                        }

                        handleScannedData(
                                scannedText.trim()
                        );
                    }

                    /**
                     * Required ZXing callback.
                     * Result points are not required by this application.
                     */
                    @Override
                    public void possibleResultPoints(
                            List<ResultPoint> resultPoints
                    ) {
                        // No additional processing required.
                    }
                }
        );
    }

    /**
     * Pass the scanned QR transaction data to the
     * server verification screen.
     */
    private void handleScannedData(String qrRawText) {

        if (qrRawText == null
                || qrRawText.trim().isEmpty()) {

            Toast.makeText(
                    this,
                    "Invalid QR data",
                    Toast.LENGTH_SHORT
            ).show();

            isProcessingScan = false;

            if (pbScanning != null) {
                pbScanning.setVisibility(View.GONE);
            }

            return;
        }

        /*
         * Stop the camera before moving to the
         * transaction verification screen.
         */
        if (barcodeScannerView != null) {
            barcodeScannerView.pause();
        }

        Intent verifyIntent = new Intent(
                this,
                VerifyTransactionActivity.class
        );

        verifyIntent.putExtra(
                Constants.EXTRA_QR_DATA,
                qrRawText.trim()
        );

        startActivity(verifyIntent);

        finish();
    }

    /**
     * Toggle the camera flashlight.
     */
    private void toggleFlashlight() {

        if (barcodeScannerView == null) {
            return;
        }

        if (isFlashOn) {

            barcodeScannerView.setTorchOff();
            isFlashOn = false;

        } else {

            barcodeScannerView.setTorchOn();
            isFlashOn = true;
        }
    }

    /**
     * Display a manual verification dialog when the operator
     * cannot scan a QR code.
     */
    private void showManualEntryDialog() {

        EditText input = new EditText(this);

        input.setHint(
                "Enter Booking ID or QR Token"
        );

        input.setPadding(
                40,
                30,
                40,
                30
        );

        new MaterialAlertDialogBuilder(this)
                .setTitle("Manual Verification Entry")
                .setMessage(
                        "Enter the prosumer booking reference or scan token:"
                )
                .setView(input)
                .setPositiveButton(
                        "Verify",
                        (dialog, which) -> {

                            String manualText =
                                    input.getText()
                                            .toString()
                                            .trim();

                            if (!manualText.isEmpty()) {

                                handleScannedData(
                                        manualText
                                );

                            } else {

                                Toast.makeText(
                                        QrScannerActivity.this,
                                        "Input cannot be empty",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .show();
    }

    /**
     * Resume the camera scanner when the activity
     * becomes visible.
     */
    @Override
    protected void onResume() {

        super.onResume();

        if (barcodeScannerView != null) {
            barcodeScannerView.resume();
        }

        isProcessingScan = false;
    }

    /**
     * Pause the camera scanner when the activity
     * is no longer visible.
     */
    @Override
    protected void onPause() {

        if (barcodeScannerView != null) {
            barcodeScannerView.pause();
        }

        super.onPause();
    }

    /**
     * Turn off the flashlight when the activity is destroyed.
     */
    @Override
    protected void onDestroy() {

        if (barcodeScannerView != null && isFlashOn) {
            barcodeScannerView.setTorchOff();
            isFlashOn = false;
        }

        super.onDestroy();
    }
}