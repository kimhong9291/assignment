package com.example.ecotrack;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import java.util.Calendar;

public class ProfileFragment extends Fragment {

    private SharedPreferences prefs;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        createNotificationChannel();

        prefs = getContext().getSharedPreferences("UserSettings", Context.MODE_PRIVATE);

        // Declarations
        Switch switchTheme = view.findViewById(R.id.switchTheme);
        Switch switchReminder = view.findViewById(R.id.switchReminder);
        Button btnReset = view.findViewById(R.id.btnReset);
        TextView btnAbout = view.findViewById(R.id.btnAbout);

        // Load saved state
        if (switchReminder != null) {
            switchReminder.setChecked(prefs.getBoolean("reminder_enabled", false));
        }

        // Theme Logic
        if (switchTheme != null) {
            switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
                AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
                );
            });
        }

        // Notification Logic
        if (switchReminder != null) {
            switchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
                prefs.edit().putBoolean("reminder_enabled", isChecked).apply();
                if (isChecked) {
                    scheduleNotification();
                    Toast.makeText(getContext(), "Reminder set for 9:00 AM daily", Toast.LENGTH_SHORT).show();
                } else {
                    cancelNotification();
                }
            });
        }

        // Reset Logic (Fixed: No duplicate variable declaration)
        if (btnReset != null) {
            btnReset.setOnClickListener(v -> {
                // Delete Databases and Prefs
                getContext().deleteDatabase("EcoTrack.db");
                getContext().getSharedPreferences("UserSettings", Context.MODE_PRIVATE).edit().clear().apply();
                getContext().getSharedPreferences("DailyTracker", Context.MODE_PRIVATE).edit().clear().apply();

                // Reset the switch visually using the variable defined at the top
                if (switchReminder != null) {
                    switchReminder.setChecked(false);
                }

                Toast.makeText(getContext(), "Full Reset Complete. Restart App.", Toast.LENGTH_LONG).show();
            });
        }

        // About App Logic
        if (btnAbout != null) {
            btnAbout.setOnClickListener(v -> {
                new androidx.appcompat.app.AlertDialog.Builder(getContext())
                    .setTitle("About EcoTrack")
                    .setMessage("Version 1.0\n\nEcoTrack helps you build sustainable habits.\n\nCreated by Deltas.")
                    .setPositiveButton("OK", null)
                    .show();
            });
        }

        return view;
    }

    // --- Helper ---

    private void scheduleNotification() {
        Intent intent = new Intent(getContext(), NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 0);

        if (Calendar.getInstance().after(calendar)) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY, pendingIntent);
    }

    private void cancelNotification() {
        Intent intent = new Intent(getContext(), NotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 0, intent, PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "DailyReminder";
            String description = "Reminds you to log eco actions";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("daily_reminder", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}