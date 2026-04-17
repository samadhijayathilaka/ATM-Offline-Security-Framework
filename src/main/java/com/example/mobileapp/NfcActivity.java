package com.example.mobileapp;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class NfcActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView tv = new TextView(this);
        tv.setText("Tap NFC Tag...");
        tv.setTextSize(20);
        tv.setPadding(50, 200, 50, 50);

        setContentView(tv);
    }
}