package com.example.stattube;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class DeepStatsFragment extends Fragment {

    private RecyclerView recyclerView;
    private StatsAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_deep_stats, container, false);
        recyclerView = view.findViewById(R.id.stats_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        ChannelViewModel viewModel = new ViewModelProvider(requireActivity()).get(ChannelViewModel.class);
        viewModel.getChannelData().observe(getViewLifecycleOwner(), channelData -> {
            if (channelData != null) {
                List<StatItem> stats = calculateStats(channelData);
                adapter = new StatsAdapter(stats);
                recyclerView.setAdapter(adapter);
            }
        });

        return view;
    }

    private List<StatItem> calculateStats(DeepAnalysis.ChannelData data) {
        List<StatItem> statList = new ArrayList<>();

        long views = safeParseLong(data.views);
        long vids = safeParseLong(data.vids);
        long subs = safeParseLong(data.subs);
        long age = safeParseLong(data.age);

        statList.add(new StatItem("Total Views", DeepAnalysis.formatCount(data.views)));
        statList.add(new StatItem("Total Videos", DeepAnalysis.formatCount(data.vids)));
        statList.add(new StatItem("Average Views per Video", DeepAnalysis.formatCount(String.valueOf(getAvg(views, vids)))));
        statList.add(new StatItem("Average Subs per Video", DeepAnalysis.formatCount(String.valueOf(getAvg(subs, vids)))));
        statList.add(new StatItem("Views per Subscriber", String.valueOf(getAvg(views, subs))));
        statList.add(new StatItem("Channel Age (Days)", String.valueOf(age)));
        statList.add(new StatItem("Average Views per Day", DeepAnalysis.formatCount(String.valueOf(getAvg(views, age)))));
        statList.add(new StatItem("Average Subs per Day", DeepAnalysis.formatCount(String.valueOf(getAvg(subs, age)))));
        statList.add(new StatItem("Est. Days per Video", String.valueOf(getAvg(age, vids))));
        statList.add(new StatItem("Estimated Impressions", DeepAnalysis.formatCount(String.valueOf(views * 20))));

        return statList;
    }

    private long safeParseLong(String number) {
        if (number == null || number.isEmpty()) return 1L; // Return 1 to avoid division by zero
        try {
            long parsed = Long.parseLong(number);
            return parsed == 0 ? 1L : parsed; // Avoid division by zero
        } catch (NumberFormatException e) {
            return 1L;
        }
    }

    private long getAvg(long numerator, long denominator) {
        if (denominator == 0) return 0;
        return numerator / denominator;
    }
}