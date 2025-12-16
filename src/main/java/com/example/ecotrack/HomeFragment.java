package com.example.ecotrack;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONObject;

public class HomeFragment extends Fragment {

    private TextView txtTotalPoints, txtDailyTip;
    private DatabaseHelper dbHelper;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        dbHelper = new DatabaseHelper(getContext());
        txtTotalPoints = view.findViewById(R.id.txtTotalPoints);
        txtDailyTip = view.findViewById(R.id.txtDailyTip);

        loadUserData();
        fetchEcoTip(); // API Integration

        return view;
    }

    private void loadUserData() {
        int total = dbHelper.getTotalPoints();
        txtTotalPoints.setText(String.valueOf(total));
    }

    // API Integration logic
    private void fetchEcoTip() {
        // Set default loading state
        txtDailyTip.setText("Loading daily wisdom...");

        new Thread(() -> {
            String tipText = "Reduce, Reuse, Recycle!"; // Fallback
            try {
                URL url = new URL("http://numbersapi.com/random/year?json");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(2000); // 2 second timeout so app doesn't freeze
                conn.setRequestMethod("GET");

                if (conn.getResponseCode() == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder result = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) result.append(line);

                    JSONObject json = new JSONObject(result.toString());
                    tipText = "Did you know? " + json.optString("text");
                }
            } catch (Exception e) {
                // Keep default tip on error
            }

            String finalTip = tipText;
            new Handler(Looper.getMainLooper()).post(() ->
                txtDailyTip.setText(finalTip)
            );
        }).start();
    }
}
