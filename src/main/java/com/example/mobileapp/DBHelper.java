package com.example.mobileapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "atm_security.db";
    private static final int DB_VERSION = 3;

    private static final String TABLE_USERS = "users";
    private static final String COL_ID = "id";
    private static final String COL_FULL_NAME = "full_name";
    private static final String COL_ACCOUNT_NUMBER = "account_number";
    private static final String COL_DEVICE_ID = "device_id";
    private static final String COL_PIN_HASH = "pin_hash";
    private static final String COL_BALANCE = "balance";

    // New table for used transactions
    private static final String TABLE_USED_TRANSACTIONS = "used_transactions";
    private static final String COL_TRANSACTION_ID = "transaction_id";
    private static final String COL_USED_AT = "used_at";

    public DBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_FULL_NAME + " TEXT NOT NULL, " +
                COL_ACCOUNT_NUMBER + " TEXT UNIQUE NOT NULL, " +
                COL_DEVICE_ID + " TEXT NOT NULL, " +
                COL_PIN_HASH + " TEXT NOT NULL, " +
                COL_BALANCE + " REAL DEFAULT 10000" +
                ")";

        String createUsedTransactionsTable = "CREATE TABLE " + TABLE_USED_TRANSACTIONS + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TRANSACTION_ID + " TEXT UNIQUE NOT NULL, " +
                COL_USED_AT + " TEXT NOT NULL" +
                ")";

        db.execSQL(createUsersTable);
        db.execSQL(createUsedTransactionsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            String createUsedTransactionsTable = "CREATE TABLE IF NOT EXISTS " + TABLE_USED_TRANSACTIONS + " (" +
                    COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COL_TRANSACTION_ID + " TEXT UNIQUE NOT NULL, " +
                    COL_USED_AT + " TEXT NOT NULL" +
                    ")";
            db.execSQL(createUsedTransactionsTable);
        }
    }

    public boolean isAccountRegistered(String accountNumber) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT " + COL_ID + " FROM " + TABLE_USERS + " WHERE " + COL_ACCOUNT_NUMBER + "=?",
                new String[]{accountNumber}
        );

        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    public boolean isDeviceRegistered(String deviceId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT " + COL_ID + " FROM " + TABLE_USERS + " WHERE " + COL_DEVICE_ID + "=?",
                new String[]{deviceId}
        );

        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    public boolean registerUser(String name, String account, String deviceId, String pinHash) {
        if (isAccountRegistered(account) || isDeviceRegistered(deviceId)) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_FULL_NAME, name);
        values.put(COL_ACCOUNT_NUMBER, account);
        values.put(COL_DEVICE_ID, deviceId);
        values.put(COL_PIN_HASH, pinHash);
        values.put(COL_BALANCE, 10000.0);

        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public boolean loginUser(String account, String pinHash) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_USERS + " WHERE " + COL_ACCOUNT_NUMBER + "=? AND " + COL_PIN_HASH + "=?",
                new String[]{account, pinHash}
        );

        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    public double getBalance(String accountNumber) {
        double balance = 0.0;
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT " + COL_BALANCE + " FROM " + TABLE_USERS + " WHERE " + COL_ACCOUNT_NUMBER + "=?",
                new String[]{accountNumber}
        );

        if (cursor.moveToFirst()) {
            balance = cursor.getDouble(0);
        }

        cursor.close();
        return balance;
    }

    public String getUserName(String accountNumber) {
        String name = "";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT " + COL_FULL_NAME + " FROM " + TABLE_USERS + " WHERE " + COL_ACCOUNT_NUMBER + "=?",
                new String[]{accountNumber}
        );

        if (cursor.moveToFirst()) {
            name = cursor.getString(0);
        }

        cursor.close();
        return name;
    }

    public boolean updateBalance(String accountNumber, double newBalance) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COL_BALANCE, newBalance);

        int result = db.update(
                TABLE_USERS,
                values,
                COL_ACCOUNT_NUMBER + "=?",
                new String[]{accountNumber}
        );

        return result > 0;
    }

    // Check whether a transaction ID was already used
    public boolean isTransactionUsed(String transactionId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT " + COL_ID + " FROM " + TABLE_USED_TRANSACTIONS + " WHERE " + COL_TRANSACTION_ID + "=?",
                new String[]{transactionId}
        );

        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    // Mark a transaction as used
    public boolean markTransactionUsed(String transactionId) {
        if (isTransactionUsed(transactionId)) {
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TRANSACTION_ID, transactionId);
        values.put(COL_USED_AT, String.valueOf(System.currentTimeMillis()));

        long result = db.insert(TABLE_USED_TRANSACTIONS, null, values);
        return result != -1;
    }
}