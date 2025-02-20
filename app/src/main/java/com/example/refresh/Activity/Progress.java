package com.example.refresh.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.refresh.Adapter.DaySection;
import com.example.refresh.Helper.DatabaseHelper;
import com.example.refresh.Database.MealsTable;
import com.example.refresh.Model.Meal;
import com.example.refresh.R;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

import io.github.luizgrp.sectionedrecyclerviewadapter.SectionedRecyclerViewAdapter;

public class Progress extends AppCompatActivity {

    private Toolbar toolbar;
    private ImageButton refreshButton;
    private ImageButton calenderButton;
    private RecyclerView recyclerViewMeals;
    private ArrayList<Meal> weekMeals;
    private SectionedRecyclerViewAdapter sectionAdapter;
    private LocalDate currentWeekStart;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        initializeUI();
        setupUI();

        // Set up the toolbar
        setSupportActionBar(toolbar);

        currentWeekStart = LocalDate.now().with(java.time.DayOfWeek.MONDAY);

        setupBottomNavigationMenu();
        setupRecyclerView();

    }

    public void initializeUI() {
        toolbar = findViewById(R.id.toolbar);
        recyclerViewMeals = findViewById(R.id.recycler_view_meals);
        refreshButton = findViewById(R.id.backArrow);
        calenderButton = findViewById(R.id.extra_button);
    }

    public void setupUI() {
        refreshButton.setImageResource(R.drawable.ic_refresh);
        calenderButton.setImageResource(R.drawable.ic_calendar);

        refreshButton.setOnClickListener(v -> updateRecyclerView());
    }

    private void setupBottomNavigationMenu() {
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_today) {
                Intent intent = new Intent(Progress.this, HomeDashboard.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_log) {
                Intent intent = new Intent(Progress.this, MealLogActivity.class);
                startActivity(intent);
                return true;
            } else if (itemId == R.id.nav_progress) {

                return true;
            }
//            else if (itemId == R.id.nav_recipes) {
//                // Handle recipes click
//                return true;
//            }

            return false;
        });

        bottomNavigationView.setSelectedItemId(R.id.nav_progress);
    }

    public void setupRecyclerView() {
        recyclerViewMeals.setLayoutManager(new LinearLayoutManager(this));

        // Create the SectionedRecyclerViewAdapter instance
        sectionAdapter = new SectionedRecyclerViewAdapter();

        recyclerViewMeals.setAdapter(sectionAdapter);

        updateRecyclerView();
        // Set the adapter to the RecyclerView
    }

    private void updateRecyclerView() {
        LocalDate weekStart = currentWeekStart;
        LocalDate weekEnd = getCurrentWeekEnd();

        // Query your database to get meals for the week
        setWeekMeals(weekStart, weekEnd);

        // Group meals by DayOfWeek
        Map<DayOfWeek, ArrayList<Meal>> mealsByDay = new EnumMap<>(DayOfWeek.class);
        for (DayOfWeek day : DayOfWeek.values()) {
            mealsByDay.put(day, new ArrayList<>());
        }
        for (Meal meal : weekMeals) {
            LocalDate mealDate = meal.getDate(); // Make sure Meal has getDate() returning LocalDate
            if (!mealDate.isBefore(weekStart) && !mealDate.isAfter(weekEnd)) {
                mealsByDay.get(mealDate.getDayOfWeek()).add(meal);
            }
        }

        // Clear current sections from the adapter
        sectionAdapter.removeAllSections();

        // Add a section for each day that has meals
        for (DayOfWeek day : DayOfWeek.values()) {
            ArrayList<Meal> dayMeals = mealsByDay.get(day);
            if (dayMeals != null && !dayMeals.isEmpty()) {
                String dayName = day.getDisplayName(TextStyle.FULL, Locale.getDefault());
                sectionAdapter.addSection(new DaySection(dayName, dayMeals));
            }
        }

        sectionAdapter.notifyDataSetChanged();
    }

    private void setWeekMeals(LocalDate weekStart, LocalDate weekEnd) {

        weekMeals = MealsTable.getMealsInRange(this, weekStart, weekEnd);

        if (weekMeals == null) {
            weekMeals = new ArrayList<>();
        }
    }

    private LocalDate getCurrentWeekEnd() {
        return currentWeekStart.plusDays(6);
    }

    private void nextWeek() {
        currentWeekStart = currentWeekStart.plusWeeks(1);
    }

    private void previousWeek() {
        currentWeekStart = currentWeekStart.minusWeeks(1);
    }

    protected void onResume() {
        super.onResume();
        bottomNavigationView.setSelectedItemId(R.id.nav_progress);
    }
}

