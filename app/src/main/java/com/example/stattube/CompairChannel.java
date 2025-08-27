package com.example.stattube;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeRequestInitializer;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CompairChannel extends AppCompatActivity {

    private static class ComparisonData {
        final Channel channel1;
        final Channel channel2;
        ComparisonData(Channel channel1, Channel channel2) {
            this.channel1 = channel1;
            this.channel2 = channel2;
        }
    }

    private static final String APPLICATION_NAME = "Statstube";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static final String API_KEY = BuildConfig.YOUTUBE_API_KEY;

    private ProgressBar progressBar;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compair_channel);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        progressBar = findViewById(R.id.progress_bar);
        recyclerView = findViewById(R.id.comparison_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Intent intent = getIntent();
        String id1 = intent.getStringExtra("CHANNEL1_ID");
        String id2 = intent.getStringExtra("CHANNEL2_ID");

        if (id1 == null || id2 == null) {
            Toast.makeText(this, "Missing channel IDs for comparison.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        new FetchComparisonTask().execute(id1, id2);
    }

    private class FetchComparisonTask extends AsyncTask<String, Void, ComparisonData> {
        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected ComparisonData doInBackground(String... ids) {
            YouTube youtube = getYouTubeService();
            if (youtube == null) return null;
            try {
                // Fetch both channels in one API call for efficiency
                String combinedIds = ids[0] + "," + ids[1];
                ChannelListResponse response = youtube.channels().list("snippet,statistics").setId(combinedIds).execute();

                if (response.getItems().size() == 2) {
                    // Ensure channels are assigned correctly based on original IDs
                    Channel chA = response.getItems().get(0);
                    Channel chB = response.getItems().get(1);
                    Channel ch1 = chA.getId().equals(ids[0]) ? chA : chB;
                    Channel ch2 = chB.getId().equals(ids[1]) ? chB : chA;
                    return new ComparisonData(ch1, ch2);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ComparisonData data) {
            progressBar.setVisibility(View.GONE);
            if (data != null && data.channel1 != null && data.channel2 != null) {
                updateUi(data);
            } else {
                Toast.makeText(CompairChannel.this, "Failed to fetch channel data.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void updateUi(ComparisonData data) {
        // Setup Headers
        setupHeader(findViewById(R.id.channel1_header), data.channel1);
        setupHeader(findViewById(R.id.channel2_header), data.channel2);

        // Prepare and set adapter for comparison list
        List<ComparisonItem> comparisonItems = prepareComparisonList(data.channel1, data.channel2);
        ComparisonAdapter adapter = new ComparisonAdapter(comparisonItems);
        recyclerView.setAdapter(adapter);
    }

    private void setupHeader(View headerView, Channel channel) {
        ImageView logo = headerView.findViewById(R.id.channel_logo);
        TextView name = headerView.findViewById(R.id.channel_name);
        TextView subs = headerView.findViewById(R.id.channel_subs);

        name.setText(channel.getSnippet().getTitle());
        String subsText = formatCount(channel.getStatistics().getSubscriberCount().toString()) + " Subs";
        subs.setText(subsText);

        // *** THIS IS THE CORRECTED LINE ***
        Picasso.get().load(channel.getSnippet().getThumbnails().getHigh().getUrl()).into(logo);
    }

    private List<ComparisonItem> prepareComparisonList(Channel c1, Channel c2) {
        List<ComparisonItem> items = new ArrayList<>();

        // Raw values
        long subs1 = c1.getStatistics().getSubscriberCount().longValue();
        long subs2 = c2.getStatistics().getSubscriberCount().longValue();
        long views1 = c1.getStatistics().getViewCount().longValue();
        long views2 = c2.getStatistics().getViewCount().longValue();
        long vids1 = c1.getStatistics().getVideoCount().longValue();
        long vids2 = c2.getStatistics().getVideoCount().longValue();
        long age1 = calculateChannelAge(c1.getSnippet().getPublishedAt().toString());
        long age2 = calculateChannelAge(c2.getSnippet().getPublishedAt().toString());

        // Calculations
        long avgViews1 = (vids1 == 0) ? 0 : views1 / vids1;
        long avgViews2 = (vids2 == 0) ? 0 : views2 / vids2;
        long viewsPerSub1 = (subs1 == 0) ? 0 : views1 / subs1;
        long viewsPerSub2 = (subs2 == 0) ? 0 : views2 / subs2;
        long viewsPerDay1 = (age1 == 0) ? 0 : views1 / age1;
        long viewsPerDay2 = (age2 == 0) ? 0 : views2 / age2;
        long subsPerDay1 = (age1 == 0) ? 0 : subs1 / age1;
        long subsPerDay2 = (age2 == 0) ? 0 : subs2 / age2;

        items.add(new ComparisonItem("Subscribers", formatCount(String.valueOf(subs1)), formatCount(String.valueOf(subs2)), subs1, subs2));
        items.add(new ComparisonItem("Total Views", formatCount(String.valueOf(views1)), formatCount(String.valueOf(views2)), views1, views2));
        items.add(new ComparisonItem("Total Videos", formatCount(String.valueOf(vids1)), formatCount(String.valueOf(vids2)), vids1, vids2));
        items.add(new ComparisonItem("Avg. Views / Video", formatCount(String.valueOf(avgViews1)), formatCount(String.valueOf(avgViews2)), avgViews1, avgViews2));
        items.add(new ComparisonItem("Views / Subscriber", String.valueOf(viewsPerSub1), String.valueOf(viewsPerSub2), viewsPerSub1, viewsPerSub2));
        items.add(new ComparisonItem("Channel Age (Days)", String.valueOf(age1), String.valueOf(age2), age1, age2));
        items.add(new ComparisonItem("Avg. Views / Day", formatCount(String.valueOf(viewsPerDay1)), formatCount(String.valueOf(viewsPerDay2)), viewsPerDay1, viewsPerDay2));
        items.add(new ComparisonItem("Avg. Subs / Day", formatCount(String.valueOf(subsPerDay1)), formatCount(String.valueOf(subsPerDay2)), subsPerDay1, subsPerDay2));

        return items;
    }

    // --- Utility Methods ---
    private long calculateChannelAge(String publishedAt) {
        try {
            // Ensure you have Java 8+ features enabled in your gradle file for this to work
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            LocalDate creationDate = LocalDate.parse(publishedAt, formatter);
            LocalDate currentDate = LocalDate.now();
            return ChronoUnit.DAYS.between(creationDate, currentDate);
        } catch (Exception e) {
            e.printStackTrace();
            return 1; // Avoid division by zero
        }
    }

    private String formatCount(String countStr) {
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

    private YouTube getYouTubeService() {
        return new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, null)
                .setApplicationName(APPLICATION_NAME)
                .setYouTubeRequestInitializer(new YouTubeRequestInitializer(API_KEY))
                .build();
    }
}