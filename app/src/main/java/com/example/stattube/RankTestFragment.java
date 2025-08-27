package com.example.stattube;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class RankTestFragment extends Fragment {

    private ProgressBar progressCircle;
    private TextView percentageText, gradeText;
    private RecyclerView testsRecyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_rank_test, container, false);

        progressCircle = view.findViewById(R.id.progressCircle);
        percentageText = view.findViewById(R.id.Percentage);
        gradeText = view.findViewById(R.id.Grade);
        testsRecyclerView = view.findViewById(R.id.tests_recycler_view);
        testsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        ChannelViewModel viewModel = new ViewModelProvider(requireActivity()).get(ChannelViewModel.class);
        viewModel.getChannelData().observe(getViewLifecycleOwner(), this::performRankTest);

        return view;
    }

    private void performRankTest(DeepAnalysis.ChannelData data) {
        if (data == null) return;

        long views = safeParseLong(data.views);
        long vids = safeParseLong(data.vids);
        long subs = safeParseLong(data.subs);
        long age = safeParseLong(data.age);

        int test1 = test1(age);
        int test2 = test2n3(getAvg(views, vids), getAvg(views, age));
        int test3 = test2n3(getAvg(subs, vids), getAvg(subs, age));
        int test4 = test4(vids, getAvg(views, subs));
        int test5 = test5(subs);
        int total = test1 + test2 + test3 + test4 + test5;

        List<StatItem> testResults = new ArrayList<>();
        testResults.add(new StatItem("Experience Test", test1 + " / 20"));
        testResults.add(new StatItem("Average View Test", test2 + " / 20"));
        testResults.add(new StatItem("Average Subscriber Test", test3 + " / 20"));
        testResults.add(new StatItem("Views per Subscriber Test", test4 + " / 20"));
        testResults.add(new StatItem("Subscriber Milestone Test", test5 + " / 20"));
        testResults.add(new StatItem("Grand Total", total + " / 100"));

        testsRecyclerView.setAdapter(new StatsAdapter(testResults));

        // Update Grade and Progress Circle
        progressCircle.setProgress(total);
        percentageText.setText(total + "%");

        String grade;
        int gradeColor;
        if (total > 80) { grade = "A+"; gradeColor = Color.parseColor("#2E7D32"); }
        else if (total > 65) { grade = "B+"; gradeColor = Color.parseColor("#4CAF50"); }
        else if (total > 40) { grade = "C+"; gradeColor = Color.parseColor("#FDD835"); }
        else if (total > 25) { grade = "D+"; gradeColor = Color.parseColor("#FF9800"); }
        else { grade = "F"; gradeColor = Color.parseColor("#D32F2F"); }
        gradeText.setText(grade);
        gradeText.setTextColor(gradeColor);
        percentageText.setTextColor(gradeColor);
    }

    // --- Calculation Helpers ---
    private long safeParseLong(String number) {
        if (number == null || number.isEmpty()) return 1L;
        try { long parsed = Long.parseLong(number); return parsed == 0 ? 1L : parsed; }
        catch (NumberFormatException e) { return 1L; }
    }
    private long getAvg(long n, long d) { return (d == 0) ? 0 : n / d; }

    private int test1(long channelAge) {
        int points = (int) (channelAge / 60);
        return Math.min(points, 20);
    }

    private int test2n3(long avg, long perDayAvg) {
        if (avg > perDayAvg) return 20;
        return (int) Math.ceil(((double) avg / perDayAvg) * 20);
    }

    private int test4(long vids, long vps) {
        double dv = vids / 10.0;
        if (dv < vps) return 20;
        return (int) Math.ceil(((double) vps / dv) * 20);
    }

    private int test5(long subs) {
        if (subs > 25_000_000) return 20;
        return (int) Math.ceil(((double) subs / 25_000_000) * 20);
    }
}