package com.example.stattube;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class StatsAdapter extends RecyclerView.Adapter<StatsAdapter.StatViewHolder> {

    private final List<StatItem> statItems;

    public StatsAdapter(List<StatItem> statItems) {
        this.statItems = statItems;
    }

    @NonNull
    @Override
    public StatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ratio_item, parent, false);
        return new StatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StatViewHolder holder, int position) {
        StatItem item = statItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return statItems.size();
    }

    static class StatViewHolder extends RecyclerView.ViewHolder {
        TextView label;
        TextView value;

        public StatViewHolder(@NonNull View itemView) {
            super(itemView);
            label = itemView.findViewById(R.id.ratio_label);
            value = itemView.findViewById(R.id.ratio_value);
        }

        void bind(StatItem item) {
            label.setText(item.getLabel());
            value.setText(item.getValue());
        }
    }
}