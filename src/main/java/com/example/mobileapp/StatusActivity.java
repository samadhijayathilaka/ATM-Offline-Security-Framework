package com.example.mobileapp;

import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class StatusActivity extends AppCompatActivity {

    TextView txtStatusInfo;
    DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        txtStatusInfo = findViewById(R.id.txtStatusInfo);
        dbHelper = new DBHelper(this);

        String accountNumber = getIntent().getStringExtra("accountNumber");
        String deviceId = Settings.Secure.getString(
                getContentResolver(),
                Settings.Secure.ANDROID_ID
        );

        String deviceStatus = "Not Verified";

        if (deviceId != null && dbHelper.isDeviceRegistered(deviceId)) {
            deviceStatus = "Verified";
        }

        String syncStatus = "Local Data Only";
        String modeStatus = "Offline Secure Mode";

        if (accountNumber == null || accountNumber.isEmpty()) {
            accountNumber = "Unknown";
        }

        txtStatusInfo.setText(
                "Account: " + accountNumber + "\n\n" +
                        "Device Status: " + deviceStatus + "\n" +
                        "Sync Status: " + syncStatus + "\n" +
                        "Mode: " + modeStatus
        );
    }
}