package com.example.mobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilAccountLogin, tilPinLogin;
    private TextInputEditText etAccountLogin, etPinLogin;
    private Button btnLoginNow;
    private TextView tvRegister;
    private DBHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        tilAccountLogin = findViewById(R.id.tilAccountLogin);
        tilPinLogin = findViewById(R.id.tilPinLogin);
        etAccountLogin = findViewById(R.id.etAccountLogin);
        etPinLogin = findViewById(R.id.etPinLogin);
        btnLoginNow = findViewById(R.id.btnLoginNow);
        tvRegister = findViewById(R.id.tvRegister);

        dbHelper = new DBHelper(this);

        btnLoginNow.setOnClickListener(v -> loginUser());

        tvRegister.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class)));
    }

    private void loginUser() {
        String account = etAccountLogin.getText().toString().trim();
        String pin = etPinLogin.getText().toString().trim();

        tilAccountLogin.setError(null);
        tilPinLogin.setError(null);

        if (account.isEmpty()) {
            tilAccountLogin.setError("Account Number is required");
            etAccountLogin.requestFocus();
            return;
        }

        if (!account.matches("\\d{12}")) {
            tilAccountLogin.setError("Account number must be exactly 12 digits");
            etAccountLogin.requestFocus();
            return;
        }

        if (pin.isEmpty()) {
            tilPinLogin.setError("PIN is required");
            etPinLogin.requestFocus();
            return;
        }

        if (!pin.matches("\\d{4}")) {
            tilPinLogin.setError("PIN must be exactly 4 digits");
            etPinLogin.requestFocus();
            return;
        }

        String pinHash = SecurityUtils.sha256(pin);

        if (dbHelper.loginUser(account, pinHash)) {
            Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            intent.putExtra("accountNumber", account);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Invalid account number or PIN", Toast.LENGTH_SHORT).show();
        }
    }
}
