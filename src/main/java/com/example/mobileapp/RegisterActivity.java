package com.example.mobileapp;

import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilName, tilAccount, tilPin, tilConfirmPin;
    private TextInputEditText etName, etAccount, etPin, etConfirmPin;
    private Button btnSubmit;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        tilName = findViewById(R.id.tilName);
        tilAccount = findViewById(R.id.tilAccount);
        tilPin = findViewById(R.id.tilPin);
        tilConfirmPin = findViewById(R.id.tilConfirmPin);

        etName = findViewById(R.id.etName);
        etAccount = findViewById(R.id.etAccount);
        etPin = findViewById(R.id.etPin);
        etConfirmPin = findViewById(R.id.etConfirmPin);
        btnSubmit = findViewById(R.id.btnSubmit);

        dbHelper = new DBHelper(this);

        btnSubmit.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String name = etName.getText().toString().trim();
        String account = etAccount.getText().toString().trim();
        String pin = etPin.getText().toString().trim();
        String confirmPin = etConfirmPin.getText().toString().trim();

        tilName.setError(null);
        tilAccount.setError(null);
        tilPin.setError(null);
        tilConfirmPin.setError(null);

        if (name.isEmpty()) {
            tilName.setError("Full Name is required");
            etName.requestFocus();
            return;
        }

        if (account.isEmpty()) {
            tilAccount.setError("Account Number is required");
            etAccount.requestFocus();
            return;
        }

        if (!account.matches("\\d{12}")) {
            tilAccount.setError("Account number must be exactly 12 digits");
            etAccount.requestFocus();
            return;
        }

        if (pin.isEmpty()) {
            tilPin.setError("PIN is required");
            etPin.requestFocus();
            return;
        }

        if (!pin.matches("\\d{4}")) {
            tilPin.setError("PIN must be exactly 4 digits");
            etPin.requestFocus();
            return;
        }

        if (confirmPin.isEmpty()) {
            tilConfirmPin.setError("Confirm PIN is required");
            etConfirmPin.requestFocus();
            return;
        }

        if (!pin.equals(confirmPin)) {
            tilConfirmPin.setError("PIN does not match");
            etConfirmPin.requestFocus();
            return;
        }

        String deviceId = Settings.Secure.getString(
                getContentResolver(), Settings.Secure.ANDROID_ID);

        if (dbHelper.isAccountRegistered(account)) {
            tilAccount.setError("This account is already registered");
            etAccount.requestFocus();
            return;
        }

        if (dbHelper.isDeviceRegistered(deviceId)) {
            Toast.makeText(this, "This device is already registered", Toast.LENGTH_LONG).show();
            return;
        }

        String pinHash = SecurityUtils.sha256(pin);
        boolean success = dbHelper.registerUser(name, account, deviceId, pinHash);

        if (success) {
            Toast.makeText(this, "Registered successfully", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show();
        }
    }
}
