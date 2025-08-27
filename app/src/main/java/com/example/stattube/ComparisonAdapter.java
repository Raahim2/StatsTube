package com.example.stattube;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ComparisonAdapter extends RecyclerView.Adapter<ComparisonAdapter.ComparisonViewHolder> {

    private final List<ComparisonItem> items;

    public ComparisonAdapter(List<ComparisonItem> items) {
        this.items = items;
    }

    @NonNull
    @Override
    public ComparisonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_comparison, parent, false);
        return new ComparisonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ComparisonViewHolder holder, int position) {
        holder.bind(items.get(position));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ComparisonViewHolder extends RecyclerView.ViewHolder {
        private final TextView channel1Value, channel2Value, statLabel;
        private final int winnerColor = Color.parseColor("#4CAF50"); // Green
        private final int loserColor = Color.parseColor("#D32F2F");  // Red
        private final int defaultColor = Color.parseColor("#212121"); // Black

        public ComparisonViewHolder(@NonNull View itemView) {
            super(itemView);
            channel1Value = itemView.findViewById(R.id.channel1_value);
            channel2Value = itemView.findViewById(R.id.channel2_value);
            statLabel = itemView.findViewById(R.id.stat_label);
        }

        void bind(ComparisonItem item) {
            statLabel.setText(item.label);
            channel1Value.setText(item.value1);
            channel2Value.setText(item.value2);

            if (item.rawValue1 > item.rawValue2) {
                channel1Value.setTextColor(winnerColor);
                channel2Value.setTextColor(loserColor);
            } else if (item.rawValue2 > item.rawValue1) {
                channel2Value.setTextColor(winnerColor);
                channel1Value.setTextColor(loserColor);
            } else { // They are equal
                channel1Value.setTextColor(defaultColor);
                channel2Value.setTextColor(defaultColor);
            }
        }
    }
}