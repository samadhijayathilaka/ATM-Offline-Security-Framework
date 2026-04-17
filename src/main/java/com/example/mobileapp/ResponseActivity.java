package com.example.mobileapp;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class ResponseActivity extends AppCompatActivity {

    private TextView tvResponseData;
    private ImageView ivResponseQR;
    private Button btnConfirm;
    private DBHelper dbHelper;

    private static final String MOBILE_SECRET = "_MOBILE_SECRET";
    private static final long TOKEN_VALID_MILLIS = 300000; // 5 minutes

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_response);

        tvResponseData = findViewById(R.id.tvResponseData);
        ivResponseQR = findViewById(R.id.ivResponseQR);
        btnConfirm = findViewById(R.id.btnConfirm);

        dbHelper = new DBHelper(this);

        String atmQrData = getIntent().getStringExtra("ATM_QR_DATA");
        String accountNumber = getIntent().getStringExtra("accountNumber");

        if (atmQrData == null || atmQrData.trim().isEmpty()) {
            tvResponseData.setText("No ATM QR data found");
            Toast.makeText(this, "No ATM QR data found", Toast.LENGTH_SHORT).show();
            disableConfirmButton();
            return;
        }

        if (accountNumber == null || accountNumber.trim().isEmpty()) {
            tvResponseData.setText("Account number not found");
            Toast.makeText(this, "Account number not found", Toast.LENGTH_SHORT).show();
            disableConfirmButton();
            return;
        }

        String[] parts = atmQrData.split("\\|");

        if (parts.length < 5) {
            tvResponseData.setText("Invalid ATM QR format");
            Toast.makeText(this, "Invalid ATM QR format", Toast.LENGTH_SHORT).show();
            disableConfirmButton();
            return;
        }

        String transactionId = parts[0];
        String qrAccountNumber = parts[1];
        String amountText = parts[2];
        String timestamp = parts[3];
        String nonce = parts[4];

        if (!accountNumber.equals(qrAccountNumber)) {
            tvResponseData.setText("Account mismatch");
            Toast.makeText(this, "Account mismatch", Toast.LENGTH_SHORT).show();
            disableConfirmButton();
            return;
        }

        if (dbHelper.isTransactionUsed(transactionId)) {
            tvResponseData.setText("This transaction was already used.");
            Toast.makeText(this, "This transaction was already used.", Toast.LENGTH_SHORT).show();
            disableConfirmButton();
            return;
        }

        String responseToken = SecurityUtils.shortToken(atmQrData + MOBILE_SECRET);

        tvResponseData.setText(
                "Transaction ID:\n" + transactionId +
                        "\n\nAccount Number:\n" + qrAccountNumber +
                        "\n\nAmount:\nRs. " + amountText +
                        "\n\nTimestamp:\n" + timestamp +
                        "\n\nNonce:\n" + nonce +
                        "\n\nResponse Token:\n" + responseToken
        );

        generateResponseQR(responseToken);

        btnConfirm.setOnClickListener(v -> {
            if (isTokenExpired(timestamp)) {
                Toast.makeText(this, "Token expired. Please scan again.", Toast.LENGTH_SHORT).show();
                disableConfirmButton();
                return;
            }
            updateLocalBalance(accountNumber, transactionId, amountText);
        });
    }

    private void disableConfirmButton() {
        if (btnConfirm != null) {
            btnConfirm.setEnabled(false);
        }
    }

    private boolean isTokenExpired(String timestamp) {
        try {
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyyMMddHHmmss");
            sdf.setLenient(false);
            long qrMillis = sdf.parse(timestamp).getTime();
            long nowMillis = System.currentTimeMillis();
            return (nowMillis - qrMillis) > TOKEN_VALID_MILLIS;
        } catch (Exception e) {
            return true;
        }
    }

    private void updateLocalBalance(String accountNumber, String transactionId, String amountText) {
        try {
            if (dbHelper.isTransactionUsed(transactionId)) {
                Toast.makeText(this, "This transaction was already used.", Toast.LENGTH_SHORT).show();
                btnConfirm.setEnabled(false);
                return;
            }

            double withdrawAmount = Double.parseDouble(amountText);
            double currentBalance = dbHelper.getBalance(accountNumber);

            if (withdrawAmount <= 0) {
                Toast.makeText(this, "Invalid withdrawal amount", Toast.LENGTH_SHORT).show();
                return;
            }

            if (withdrawAmount > currentBalance) {
                Toast.makeText(this, "Insufficient Balance", Toast.LENGTH_SHORT).show();
                return;
            }

            double newBalance = currentBalance - withdrawAmount;
            boolean balanceUpdated = dbHelper.updateBalance(accountNumber, newBalance);

            if (!balanceUpdated) {
                Toast.makeText(this, "Local balance update failed", Toast.LENGTH_SHORT).show();
                return;
            }

            boolean marked = dbHelper.markTransactionUsed(transactionId);
            if (!marked) {
                Toast.makeText(this, "Transaction already marked as used", Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(this, "Local balance updated successfully", Toast.LENGTH_LONG).show();
            btnConfirm.setEnabled(false);

        } catch (Exception e) {
            Toast.makeText(this, "Balance update error", Toast.LENGTH_SHORT).show();
        }
    }

    private void generateResponseQR(String text) {
        QRCodeWriter writer = new QRCodeWriter();

        try {
            int size = 600;
            BitMatrix bitMatrix = writer.encode(text, BarcodeFormat.QR_CODE, size, size);
            Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565);

            for (int x = 0; x < size; x++) {
                for (int y = 0; y < size; y++) {
                    bitmap.setPixel(
                            x,
                            y,
                            bitMatrix.get(x, y)
                                    ? android.graphics.Color.BLACK
                                    : android.graphics.Color.WHITE
                    );
                }
            }

            ivResponseQR.setImageBitmap(bitmap);

        } catch (WriterException e) {
            Toast.makeText(this, "QR generation failed", Toast.LENGTH_SHORT).show();
        }
    }
}