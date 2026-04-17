package com.example.mobileapp;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class TokenActivity extends AppCompatActivity {

    private TextView txtToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_token);

        txtToken = findViewById(R.id.txtToken);

        String accountNumber = getIntent().getStringExtra("accountNumber");

        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            txtToken.setText("TOKENERR");
            Toast.makeText(this, "Account number not found", Toast.LENGTH_SHORT).show();
            return;
        }

        String token = generateToken(accountNumber);
        txtToken.setText(token);
    }

    private String generateToken(String accountNumber) {
        try {
            String rawData = accountNumber + "|" + System.currentTimeMillis();
            return SecurityUtils.shortToken(rawData);
        } catch (Exception e) {
            return "TOKENERR";
        }
    }
}