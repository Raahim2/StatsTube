package com.example.stattube;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
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
import com.google.api.services.youtube.model.PlaylistItem;
import com.google.api.services.youtube.model.PlaylistItemListResponse;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChannelStastics extends AppCompatActivity {

    private static class ChannelDetails {
        final Channel channel;
        final List<Video> recentVideos;
        ChannelDetails(Channel channel, List<Video> recentVideos) {
            this.channel = channel;
            this.recentVideos = recentVideos;
        }
    }

    private static final String APPLICATION_NAME = "Statstube";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static final String API_KEY = BuildConfig.YOUTUBE_API_KEY;

    private String channelId;
    private Channel channel; // Keep a reference for passing to other activities

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_channel_stastics);

        // Toolbar Setup
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Get Channel ID from Intent
        Intent intent = getIntent();
        channelId = intent.getStringExtra("CHANNEL_ID");
        String channelName = intent.getStringExtra("CHANNEL_NAME");

        if (channelId == null || channelId.isEmpty()) {
            Toast.makeText(this, "Channel ID is missing.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Set initial title
        ((TextView) findViewById(R.id.channel_name)).setText(channelName);

        // Fetch all data
        new FetchChannelStatsTask().execute(channelId);
    }

    private class FetchChannelStatsTask extends AsyncTask<String, Void, ChannelDetails> {

        @Override
        protected ChannelDetails doInBackground(String... params) {
            String channelId = params[0];
            YouTube youtube = getYouTubeService(API_KEY);
            if (youtube == null) return null;

            try {
                // 1. Fetch Channel Details
                YouTube.Channels.List request = youtube.channels().list("snippet,contentDetails,statistics").setId(channelId);
                ChannelListResponse response = request.execute();
                if (response.getItems().isEmpty()) return null;
                Channel channel = response.getItems().get(0);

                // 2. Fetch Recent Videos (Optimized)
                List<Video> recentVideos = getRecentVideos(youtube, channel, 5);

                return new ChannelDetails(channel, recentVideos);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(ChannelDetails result) {
            if (result != null) {
                ChannelStastics.this.channel = result.channel; // Save channel reference
                updateUi(result);
            } else {
                Toast.makeText(ChannelStastics.this, "Failed to fetch channel statistics.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void updateUi(ChannelDetails details) {
        // Update Header
        ((TextView) findViewById(R.id.channel_name)).setText(details.channel.getSnippet().getTitle());
        String subsText = formatCount(details.channel.getStatistics().getSubscriberCount()) + " Subscribers";
        ((TextView) findViewById(R.id.channel_subscribers)).setText(subsText);
        Picasso.get().load(details.channel.getSnippet().getThumbnails().getHigh().getUrl()).into((ImageView) findViewById(R.id.channel_logo));

        // Update Statistics Card
        BigInteger totalViews = details.channel.getStatistics().getViewCount();
        BigInteger videoCount = details.channel.getStatistics().getVideoCount();
        BigInteger avgViews = (videoCount.equals(BigInteger.ZERO)) ? BigInteger.ZERO : totalViews.divide(videoCount);

        setupStatItem(findViewById(R.id.total_views_stat), R.drawable.view, "Total Views", formatCount(totalViews));
        setupStatItem(findViewById(R.id.total_videos_stat), R.drawable.video, "Total Videos", formatCount(videoCount));
        setupStatItem(findViewById(R.id.avg_views_stat), R.drawable.share, "Avg. Views", formatCount(avgViews));

        // Setup RecyclerView for Recent Videos
        RecyclerView recyclerView = findViewById(R.id.recent_videos_recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setHasFixedSize(false); // Important for NestedScrollView
        ChannelVideoAdapter adapter = new ChannelVideoAdapter(this, details.recentVideos);
        recyclerView.setAdapter(adapter);
    }

    private void setupStatItem(View statView, int iconRes, String label, String value) {
        ((ImageView) statView.findViewById(R.id.stat_icon)).setImageResource(iconRes);
        ((TextView) statView.findViewById(R.id.stat_label)).setText(label);
        ((TextView) statView.findViewById(R.id.stat_value)).setText(value);
    }

    // --- YouTube API Helpers (Optimized) ---

    private List<Video> getRecentVideos(YouTube youtubeService, Channel channel, long maxResults) throws IOException {
        String uploadsPlaylistId = channel.getContentDetails().getRelatedPlaylists().getUploads();

        // Step 1: Get the video IDs from the uploads playlist
        PlaylistItemListResponse playlistResponse = youtubeService.playlistItems()
                .list("contentDetails")
                .setPlaylistId(uploadsPlaylistId)
                .setMaxResults(maxResults)
                .execute();

        List<String> videoIds = new ArrayList<>();
        for (PlaylistItem item : playlistResponse.getItems()) {
            videoIds.add(item.getContentDetails().getVideoId());
        }

        if (videoIds.isEmpty()) {
            return new ArrayList<>(); // Return empty list if no videos
        }

        // Step 2: Make ONE API call to get details for all videos at once
        VideoListResponse videoListResponse = youtubeService.videos()
                .list("snippet,statistics")
                .setId(String.join(",", videoIds))
                .execute();

        return videoListResponse.getItems();
    }


    // --- Intent Handlers for "More Options" ---

    private void launchDeepAnalysis(String infoType) {
        if (channel == null) {
            Toast.makeText(this, "Channel data not loaded yet.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, DeepAnalysis.class); // Assuming DeepAnalysis exists
        intent.putExtra("INFO", infoType);
        intent.putExtra("CHANNEL_ID", channel.getId());
        intent.putExtra("CHANNEL_NAME", channel.getSnippet().getTitle());
        intent.putExtra("CHANNEL_LOGO", channel.getSnippet().getThumbnails().getHigh().getUrl());
        intent.putExtra("CHANNEL_VIDS", channel.getStatistics().getVideoCount().toString());
        intent.putExtra("CHANNEL_SUBS", channel.getStatistics().getSubscriberCount().toString());
        intent.putExtra("CHANNEL_VIEWS", channel.getStatistics().getViewCount().toString());
        startActivity(intent);
    }

    public void showA(View v) { launchDeepAnalysis("OP1"); }
    public void showB(View v) { launchDeepAnalysis("OP2"); }
    public void showC(View v) { launchDeepAnalysis("OP3"); }
    public void showD(View v) { launchDeepAnalysis("OP4"); }

    public void Allvideo(View V) {
        if (channelId == null) return;
        Intent intent = new Intent(this, AllVideos.class); // Assuming AllVideos exists
        intent.putExtra("CHANNEL_ID", channelId);
        intent.putExtra("CHANNEL_NAME", channel.getSnippet().getTitle());
        startActivity(intent);
    }


    // --- Utility Methods ---

    public static String formatCount(BigInteger count) {
        if (count == null) return "0";
        long longCount = count.longValue();
        if (longCount >= 1_000_000_000) return String.format(Locale.US, "%.1fB", longCount / 1_000_000_000.0);
        if (longCount >= 1_000_000) return String.format(Locale.US, "%.1fM", longCount / 1_000_000.0);
        if (longCount >= 1_000) return String.format(Locale.US, "%.1fK", longCount / 1_000.0);
        return String.valueOf(longCount);
    }

    public static YouTube getYouTubeService(String apiKey) {
        try {
            return new YouTube.Builder(new NetHttpTransport(), new GsonFactory(), null)
                    .setApplicationName(APPLICATION_NAME)
                    .setYouTubeRequestInitializer(new YouTubeRequestInitializer(apiKey))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}