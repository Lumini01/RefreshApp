package com.example.refresh;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class HomeDashboard extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        // Handle system bars insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Setup Toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Setup Bottom Navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_today) {
                // Handle home click
                return true;
            } else if (itemId == R.id.nav_progress) {
                // Handle progress click
                return true;
            } else if (itemId == R.id.nav_log) {
                // Handle log click
                return true;
            } else if (itemId == R.id.nav_suggestions) {
                // Handle suggestions click
                return true;
            } else if (itemId == R.id.nav_recipes) {
                // Handle recipes click
                return true;
            }
            return false;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the toolbar if present
        getMenuInflater().inflate(R.menu.main_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle menu item clicks
        int itemId = item.getItemId();
        if (itemId == R.id.action_profile) {
            // Handle profile action click
            return true;
        } else if (itemId == R.id.action_notifications) {
            // Handle notifications action click
            return true;
        } else if (itemId == R.id.action_menu) {
            // Handle navigation drawer action click
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
