package com.example.stattube;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.squareup.picasso.Picasso;
import java.io.Serializable;
import java.util.Locale;

public class DeepAnalysis extends AppCompatActivity {

    // Simple data class to hold channel info
    public static class ChannelData implements Serializable {
        String id, name, logoUrl, subs, views, vids, age;
    }

    private ViewPager2 viewPager;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deep_analysis);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // 1. Get data from Intent and create a ChannelData object
        Intent intent = getIntent();
        ChannelData channelData = new ChannelData();
        channelData.id = intent.getStringExtra("CHANNEL_ID");
        channelData.name = intent.getStringExtra("CHANNEL_NAME");
        channelData.logoUrl = intent.getStringExtra("CHANNEL_LOGO");
        channelData.subs = intent.getStringExtra("CHANNEL_SUBS");
        channelData.views = intent.getStringExtra("CHANNEL_VIEWS");
        channelData.vids = intent.getStringExtra("CHANNEL_VIDS");
        channelData.age = intent.getStringExtra("CHANNEL_AGE"); // assuming this is passed from ChannelStastics

        // 2. Set up the header UI
        getSupportActionBar().setTitle(channelData.name);
        ((TextView)findViewById(R.id.channel_name)).setText(channelData.name);
        ((TextView)findViewById(R.id.channel_subscribers)).setText(formatCount(channelData.subs));
        Picasso.get().load(channelData.logoUrl).into((ImageView) findViewById(R.id.channel_logo));

        // 3. Store data in a ViewModel to share with Fragments
        ChannelViewModel viewModel = new ViewModelProvider(this).get(ChannelViewModel.class);
        viewModel.setChannelData(channelData);

        // 4. Set up ViewPager and TabLayout
        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tab_layout);
        viewPager.setAdapter(new AnalysisPagerAdapter(this));

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0: tab.setText("Deep Stats"); break;
                case 1: tab.setText("Rank Test"); break;
                case 2: tab.setText("Prediction"); break;
                case 3: tab.setText("Compare"); break;
            }
        }).attach();

        // 5. Select the initial tab based on the intent extra
        String initialTab = intent.getStringExtra("INFO");
        if (initialTab != null) {
            switch (initialTab) {
                case "OP2": viewPager.setCurrentItem(1); break;
                case "OP3": viewPager.setCurrentItem(2); break;
                case "OP4": viewPager.setCurrentItem(3); break;
                default: viewPager.setCurrentItem(0); break; // OP1
            }
        }
    }

    // --- ViewPager Adapter ---
    private static class AnalysisPagerAdapter extends FragmentStateAdapter {
        public AnalysisPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            switch (position) {
                case 1: return new RankTestFragment();
                case 2: return new PredictionFragment();
                case 3: return new CompareFragment();
                default: return new DeepStatsFragment(); // Position 0
            }
        }

        @Override
        public int getItemCount() {
            return 4;
        }
    }

    // --- Static helper for formatting ---
    public static String formatCount(String countStr) {
        if (countStr == null) return "0";
        try {
            long count = Long.parseLong(countStr);
            if (count >= 1_000_000_000) return String.format(Locale.US, "%.1fB", count / 1_000_000_000.0);
            if (count >= 1_000_000) return String.format(Locale.US, "%.1fM", count / 1_000_000.0);
            if (count >= 1_000) return String.format(Locale.US, "%.1fK", count / 1_000.0);
            return String.valueOf(count);
        } catch (NumberFormatException e) {
            return "N/A";
        }
    }
}