package com.example.ecotrack;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TrackerFragment extends Fragment {

    private LinearLayout taskContainer;
    private DatabaseHelper dbHelper;
    private SharedPreferences prefs;

    // Tasks and Points config
    private final String[] tasks = {
        "Used a reusable bottle",
        "Ate a meat-free meal",
        "Took public transport",
        "Recycled waste",
        "Turned off unused lights"
    };
    private final int[] points = {10, 25, 20, 15, 5};

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tracker, container, false);

        dbHelper = new DatabaseHelper(getContext());
        // Matches the file name used in ProfileFragment for resetting
        prefs = getContext().getSharedPreferences("DailyTracker", Context.MODE_PRIVATE);
        taskContainer = view.findViewById(R.id.taskContainer);

        loadTasks();

        return view;
    }

    private void loadTasks() {
        taskContainer.removeAllViews(); // Prevent duplicates
        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        for (int i = 0; i < tasks.length; i++) {
            CheckBox checkBox = new CheckBox(getContext());
            checkBox.setText(tasks[i] + " (+" + points[i] + " pts)");
            checkBox.setTextSize(16);
            checkBox.setPadding(0, 16, 0, 16);

            // Restore state from memory
            boolean isDone = prefs.getBoolean(today + "_task_" + i, false);
            checkBox.setChecked(isDone);

            // let the user see it's checked.
            checkBox.setEnabled(true);

            int finalI = i;
            checkBox.setOnClickListener(v -> {
                boolean isChecked = checkBox.isChecked();

                // Save the new state
                prefs.edit().putBoolean(today + "_task_" + finalI, isChecked).apply();

                if (isChecked) {
                    // Only add points if checking
                    dbHelper.addPoints(points[finalI]);
                    Toast.makeText(getContext(), "Points Added!", Toast.LENGTH_SHORT).show();
                } else {
                    // subtract points here if you added a subtract method
                    Toast.makeText(getContext(), "Task undone", Toast.LENGTH_SHORT).show();
                }
            });

            taskContainer.addView(checkBox);
        }
    }
}