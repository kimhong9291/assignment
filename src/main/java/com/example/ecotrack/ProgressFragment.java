package com.example.ecotrack;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class ProgressFragment extends Fragment {

    private DatabaseHelper dbHelper;
    // UI Components
    private TextView txtStreak, txtTodayPoints, txtLevel;
    private ProgressBar progressBarToday;
    private LinearLayout chartContainer;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // 1. Inflate Layout
        View view = inflater.inflate(R.layout.fragment_progress, container, false);

        // 2. Initialize Database Helper
        dbHelper = new DatabaseHelper(getContext());

        // 3. Find Views
        txtStreak = view.findViewById(R.id.txtStreak);
        txtTodayPoints = view.findViewById(R.id.txtTodayPoints);
        progressBarToday = view.findViewById(R.id.progressBarToday);
        txtLevel = view.findViewById(R.id.txtLevel);
        chartContainer = view.findViewById(R.id.chartContainer);

        // 4. Load Data
        loadStatistics();
        loadWeeklyChart();

        return view;
    }

    private void loadStatistics() {
        if (getContext() == null) return; // Prevent crash

        int todayPoints = dbHelper.getTodayPoints();
        int totalPoints = dbHelper.getTotalPoints();

        // --- Gamification Logic ---
        String level = "Seedling";
        if (totalPoints > 1000) level = "Forest Guardian";
        else if (totalPoints > 500) level = "Guardian Tree";
        else if (totalPoints > 100) level = "Sapling";

        // --- Update UI ---
        if (txtLevel != null) {
            txtLevel.setText(level);
        }

        if (txtStreak != null) {
            // If points > 0 today, streak is active
            int streak = (todayPoints > 0) ? 1 : 0;
            txtStreak.setText(streak + " Day Streak");
        }

        if (txtTodayPoints != null) {
            txtTodayPoints.setText(todayPoints + " / 100 Pts");
        }

        if (progressBarToday != null) {
            progressBarToday.setProgress(Math.min(todayPoints, 100));
        }
    }

    private void loadWeeklyChart() {
        if (chartContainer == null || getContext() == null) return;

        chartContainer.removeAllViews(); // Clear old bars to prevent duplicates

        int[] weeklyData = dbHelper.getLast7DaysPoints(); // Ensure this method exists in DatabaseHelper

        // Find max value to scale the bars (prevent them from being too small/large)
        int maxPoints = 100; // Default goal
        for (int p : weeklyData) {
            if (p > maxPoints) maxPoints = p;
        }

        // --- Draw 7 Bars ---
        for (int i = 0; i < 7; i++) {
            int points = weeklyData[i];

            // 1. Create Column (Holds Bar + Label)
            LinearLayout column = new LinearLayout(getContext());
            column.setOrientation(LinearLayout.VERTICAL);
            // Weight 1 ensures all 7 bars are equal
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f);
            params.setMargins(4, 0, 4, 0); // Spacing
            column.setLayoutParams(params);
            column.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);

            // 2. Create Bar
            View bar = new View(getContext());

            // Calculate height percentage: (Points / Max) * Available Height
            int heightInPixels = (int) ((points / (float) maxPoints) * 350);
            if (heightInPixels < 15) heightInPixels = 15; // Minimum height so 0 isn't invisible

            LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, heightInPixels);
            bar.setLayoutParams(barParams);

            // Set Color: Primary Green if points exist, Grey if 0
            if (points > 0) {
                bar.setBackgroundColor(getResources().getColor(R.color.seed, null));
            } else {
                // Uses grey
                bar.setBackgroundColor(0xFFE0E0E0);
            }

            // 3. Create Label (M, T, W...)
            TextView label = new TextView(getContext());
            label.setText(getDayLabel(i));
            label.setTextSize(10f);
            label.setGravity(Gravity.CENTER);
            // Use 'black' resource which auto-switches to white in dark mode
            label.setTextColor(getResources().getColor(R.color.black, null));

            // Add views to column
            column.addView(bar);
            column.addView(label);

            // Add column to chart
            chartContainer.addView(column);
        }
    }

    // Helper: Returns "M", "T", "W" based on the day index (0 is 6 days ago, 6 is Today)
    private String getDayLabel(int index) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -(6 - index));
        SimpleDateFormat sdf = new SimpleDateFormat("E", Locale.getDefault()); // E = Mon
        return sdf.format(cal.getTime()).substring(0, 1); // Get first letter
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh data every time user returns to this tab
        loadStatistics();
        loadWeeklyChart();
    }
}