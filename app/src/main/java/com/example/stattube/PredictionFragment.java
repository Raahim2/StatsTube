package com.example.stattube;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.textfield.TextInputEditText;
import java.util.Locale;

public class PredictionFragment extends Fragment {

    private TextInputEditText currentSubsText, targetSubsText, timePeriodText;
    private TextView estimatedDaysValue, estimatedSubsValue;
    private CardView resultsCard;
    private DeepAnalysis.ChannelData channelData;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_prediction, container, false);

        currentSubsText = view.findViewById(R.id.current_subs_edit_text);
        targetSubsText = view.findViewById(R.id.target_subs_edit_text);
        timePeriodText = view.findViewById(R.id.time_period_edit_text);
        Button predictButton = view.findViewById(R.id.predict_button);
        resultsCard = view.findViewById(R.id.results_card);
        
        // Use include IDs to find views in results card
        View estimatedDaysItemView = view.findViewById(R.id.estimated_days_item);
        estimatedDaysValue = estimatedDaysItemView.findViewById(R.id.ratio_value);
        ((TextView) estimatedDaysItemView.findViewById(R.id.ratio_label)).setText("Time to reach target");
        
        View estimatedSubsItemView = view.findViewById(R.id.estimated_subs_item);
        estimatedSubsValue = estimatedSubsItemView.findViewById(R.id.ratio_value);
        ((TextView) estimatedSubsItemView.findViewById(R.id.ratio_label)).setText("Subs after time period");

        ChannelViewModel viewModel = new ViewModelProvider(requireActivity()).get(ChannelViewModel.class);
        viewModel.getChannelData().observe(getViewLifecycleOwner(), data -> {
            this.channelData = data;
            if (data != null) {
                currentSubsText.setText(DeepAnalysis.formatCount(data.subs));
            }
        });

        predictButton.setOnClickListener(v -> predict());

        return view;
    }

    private void predict() {
        if (channelData == null) {
            Toast.makeText(getContext(), "Channel data not loaded", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            long currentSubs = Long.parseLong(channelData.subs);
            long age = Long.parseLong(channelData.age);
            long dailyIncrease = (age > 0) ? currentSubs / age : 0;

            if (dailyIncrease == 0) {
                Toast.makeText(getContext(), "Cannot predict with zero daily growth.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Calculation for estimated days
            long targetSubs = Long.parseLong(targetSubsText.getText().toString());
            long remainingSubs = targetSubs - currentSubs;
            if (remainingSubs > 0) {
                long estimatedDays = remainingSubs / dailyIncrease;
                estimatedDaysValue.setText(formatDays((int) estimatedDays));
            } else {
                estimatedDaysValue.setText("Target already reached");
            }

            // Calculation for estimated subs in X days
            int days = Integer.parseInt(timePeriodText.getText().toString());
            long estimatedSubs = currentSubs + (days * dailyIncrease);
            estimatedSubsValue.setText(DeepAnalysis.formatCount(String.valueOf(estimatedSubs)));

            resultsCard.setVisibility(View.VISIBLE);

        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Please enter valid numbers", Toast.LENGTH_SHORT).show();
        }
    }

    private String formatDays(int numDays) {
        if (numDays < 0) return "N/A";
        int years = numDays / 365;
        int months = (numDays % 365) / 30;
        int days = (numDays % 365) % 30;

        StringBuilder sb = new StringBuilder();
        if (years > 0) sb.append(years).append("y ");
        if (months > 0) sb.append(months).append("m ");
        if (days > 0 || sb.length() == 0) sb.append(days).append("d");
        return sb.toString().trim();
    }
}