package com.example.mobileapp;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.Parcelable;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.nio.charset.StandardCharsets;

public class ScanActivity extends AppCompatActivity {

    private Button btnScanQR;
    private TextView tvScannedData, tvNfcStatus;

    private NfcAdapter nfcAdapter;
    private PendingIntent pendingIntent;
    private IntentFilter[] intentFiltersArray;

    // QR SCANNER
    private final ActivityResultLauncher<Intent> barcodeLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        IntentResult intentResult = IntentIntegrator.parseActivityResult(
                                result.getResultCode(),
                                result.getData()
                        );

                        if (intentResult != null && intentResult.getContents() != null) {
                            processATMData(intentResult.getContents(), "QR");
                        } else {
                            tvScannedData.setText("Scan cancelled");
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        btnScanQR = findViewById(R.id.btnScanQR);
        tvScannedData = findViewById(R.id.tvScannedData);
        tvNfcStatus = findViewById(R.id.tvNfcStatus);

        // CAMERA PERMISSION
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA}, 100);
        }

        // NFC SETUP
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);

        if (nfcAdapter == null) {
            tvNfcStatus.setText("❌ NFC not supported");
        } else if (!nfcAdapter.isEnabled()) {
            tvNfcStatus.setText("⚠️ NFC is OFF. Please enable");
        } else {
            tvNfcStatus.setText("✅ NFC Ready - Tap phone on ATM");
        }

        pendingIntent = PendingIntent.getActivity(
                this,
                0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP),
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE
        );

        IntentFilter ndef = new IntentFilter(NfcAdapter.ACTION_NDEF_DISCOVERED);
        try {
            ndef.addDataType("*/*");
        } catch (IntentFilter.MalformedMimeTypeException e) {
            e.printStackTrace();
        }

        intentFiltersArray = new IntentFilter[]{ndef};

        btnScanQR.setOnClickListener(v -> startQRScanner());

        handleNfcIntent(getIntent());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null) {
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (nfcAdapter != null) {
            nfcAdapter.disableForegroundDispatch(this);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleNfcIntent(intent);
    }

    // QR START
    private void startQRScanner() {
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setPrompt("Scan ATM QR Code");
        integrator.setBeepEnabled(true);
        integrator.setOrientationLocked(true);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        

        barcodeLauncher.launch(integrator.createScanIntent());
    }

    // NFC HANDLER
    private void handleNfcIntent(Intent intent) {
        if (intent == null) return;

        String action = intent.getAction();

        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)) {

            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);

            if (rawMsgs != null && rawMsgs.length > 0) {
                NdefMessage msg = (NdefMessage) rawMsgs[0];
                NdefRecord record = msg.getRecords()[0];

                String data = readText(record);

                processATMData(data, "NFC");
            } else {
                Toast.makeText(this, "Empty NFC tag", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String readText(NdefRecord record) {
        try {
            byte[] payload = record.getPayload();
            int langLength = payload[0] & 0x3F;

            return new String(payload, langLength + 1,
                    payload.length - langLength - 1, StandardCharsets.UTF_8);

        } catch (Exception e) {
            return "";
        }
    }

    // COMMON PROCESS
    private void processATMData(String data, String source) {
        if (data == null || data.isEmpty()) {
            Toast.makeText(this, "Invalid " + source, Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate ATM format
        String[] parts = data.split("\\|");
        if (parts.length < 5) {
            Toast.makeText(this, "Invalid ATM data format", Toast.LENGTH_SHORT).show();
            return;
        }

        String accountNumber = getIntent().getStringExtra("accountNumber");

        if (accountNumber == null || accountNumber.isEmpty()) {
            Toast.makeText(this, "Session expired. Please login again.", Toast.LENGTH_LONG).show();
            return;
        }

        tvScannedData.setText(data);
        Toast.makeText(this, source + " data received", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, ResponseActivity.class);
        intent.putExtra("ATM_QR_DATA", data);
        intent.putExtra("accountNumber", accountNumber);
        startActivity(intent);
    }

    // PERMISSION RESULT
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);

        if (requestCode == 100 && results.length > 0
                && results[0] == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show();
        }
    }
}