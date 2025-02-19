package com.example.refresh;

import android.content.SharedPreferences;

import com.example.refresh.Database.DatabaseHelper;
import com.example.refresh.Database.Tables.UsersTable;

public class MyApplication extends android.app.Application {
    private static MyApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();

        // Initialize global state or resources here
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        sharedPreferences.edit().putBoolean("isFirstLaunch", true).apply();

        if (!loggedUserExistsInDB())
            sharedPreferences.edit().putInt("loggedUserID", -1).apply();
    }

    public static MyApplication getInstance() {
        return instance;
    }

    public int getLoggedUserID() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        return sharedPreferences.getInt("loggedUserID", -1);
    }

    public boolean loggedUserExistsInDB() {
        SharedPreferences sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE);
        int loggedUserID = sharedPreferences.getInt("loggedUserID", -1);

        DatabaseHelper dbHelper = new DatabaseHelper(this);

        return loggedUserID != -1 &&dbHelper.existsInDB(DatabaseHelper.Tables.USERS, UsersTable.Columns.ID, String.valueOf(loggedUserID)) != -1;
    }
}
