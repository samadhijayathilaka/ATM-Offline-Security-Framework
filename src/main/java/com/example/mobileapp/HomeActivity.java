package com.example.mobileapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.text.DecimalFormat;

public class HomeActivity extends AppCompatActivity {

    TextView txtAccountNumber, txtUserName, txtUserNameFull, txtBalance;
    CardView cardScanQR, cardTapNFC;
    DBHelper dbHelper;
    String accountNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        txtAccountNumber = findViewById(R.id.txtAccountNumber);
        txtUserName = findViewById(R.id.txtUserName);
        txtUserNameFull = findViewById(R.id.txtUserNameFull);
        txtBalance = findViewById(R.id.txtBalance);
        cardScanQR = findViewById(R.id.cardScanQR);
        cardTapNFC = findViewById(R.id.cardTapNFC);

        dbHelper = new DBHelper(this);

        accountNumber = getIntent().getStringExtra("accountNumber");

        if (accountNumber == null || accountNumber.isEmpty()) {
            Toast.makeText(this, "Account number not found", Toast.LENGTH_SHORT).show();
            txtAccountNumber.setText("**** **** **** 0000");
            txtUserName.setText("Unknown User");
            txtBalance.setText("Rs. 0.00");
        } else {
            loadUserData(accountNumber);
        }

        // Scan QR
        cardScanQR.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, ScanActivity.class);
            intent.putExtra("accountNumber", accountNumber);
            startActivity(intent);
        });

        // NFC
        cardTapNFC.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, NfcActivity.class);
            intent.putExtra("accountNumber", accountNumber);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (accountNumber != null && !accountNumber.isEmpty()) {
            loadUserData(accountNumber);
        }
    }

    private void loadUserData(String accountNumber) {
        String maskedAccount = maskAccountNumber(accountNumber);
        txtAccountNumber.setText(maskedAccount);

        try {
            String fullName = dbHelper.getUserName(accountNumber);
            double balance = dbHelper.getBalance(accountNumber);

            DecimalFormat df = new DecimalFormat("#,##0.00");

            if (fullName == null || fullName.isEmpty()) {
                fullName = "User";
            }

            txtUserName.setText("Welcome back,");
            txtUserNameFull.setText(fullName);
            txtBalance.setText("Rs. " + df.format(balance));

        } catch (Exception e) {
            txtUserName.setText("Welcome back,");
            txtUserNameFull.setText("User");
            txtBalance.setText("Rs. 0.00");
            Toast.makeText(this, "Load error", Toast.LENGTH_SHORT).show();
        }
    }

    private String maskAccountNumber(String accountNumber) {
        if (accountNumber != null && accountNumber.length() >= 4) {
            String lastFour = accountNumber.substring(accountNumber.length() - 4);
            return "**** **** **** " + lastFour;
        }
        return accountNumber != null ? accountNumber : "";
    }
}