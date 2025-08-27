package com.example.stattube;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Pie;
import com.anychart.enums.Align;
import com.anychart.enums.LegendLayout;
import com.google.android.material.tabs.TabLayout;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeRequestInitializer;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;
import com.squareup.picasso.Picasso;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class VideoStatistics extends AppCompatActivity {

    // A simple wrapper class to hold results from the background task
    private static class VideoDetails {
        final Video video;
        final Channel channel;

        VideoDetails(Video video, Channel channel) {
            this.video = video;
            this.channel = channel;
        }
    }

    private static final String APPLICATION_NAME = "Statstube";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static final String API_KEY = BuildConfig.YOUTUBE_API_KEY;

    private String videoId;
    private View videoStatisticsLayout, graphicalAnalysisLayout, rankTestLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_statistics);

        // Toolbar Setup
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // Get Video ID from Intent
        Intent intent = getIntent();
        videoId = intent.getStringExtra("VIDEO_ID");
        if (videoId == null || videoId.isEmpty()) {
            Toast.makeText(this, "Video ID is missing.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // Initialize Layouts and Tabs
        videoStatisticsLayout = findViewById(R.id.video_statistics);
        graphicalAnalysisLayout = findViewById(R.id.graphical_analysis);
        rankTestLayout = findViewById(R.id.rank_test);
        setupTabLayout();

        // Fetch data
        new FetchVideoAndChannelStatsTask().execute(videoId);
    }

    private void setupTabLayout() {
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(@NonNull TabLayout.Tab tab) {
                videoStatisticsLayout.setVisibility(View.GONE);
                graphicalAnalysisLayout.setVisibility(View.GONE);
                rankTestLayout.setVisibility(View.GONE);

                switch (tab.getPosition()) {
                    case 0:
                        videoStatisticsLayout.setVisibility(View.VISIBLE);
                        break;
                    case 1:
                        graphicalAnalysisLayout.setVisibility(View.VISIBLE);
                        break;
                    case 2:
                        rankTestLayout.setVisibility(View.VISIBLE);
                        break;
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private class FetchVideoAndChannelStatsTask extends AsyncTask<String, Void, VideoDetails> {
        @Override
        protected VideoDetails doInBackground(String... params) {
            String videoId = params[0];
            YouTube youtube = getYouTubeService(API_KEY);
            if (youtube == null) return null;

            try {
                // 1. Fetch Video Details
                YouTube.Videos.List videoRequest = youtube.videos().list("snippet,statistics").setId(videoId);
                VideoListResponse videoResponse = videoRequest.execute();
                if (videoResponse.getItems().isEmpty()) {
                    return null; // Video not found
                }
                Video video = videoResponse.getItems().get(0);

                // 2. Fetch Channel Details using channelId from the video
                String channelId = video.getSnippet().getChannelId();
                YouTube.Channels.List channelRequest = youtube.channels().list("statistics").setId(channelId);
                ChannelListResponse channelResponse = channelRequest.execute();
                if (channelResponse.getItems().isEmpty()) {
                    return new VideoDetails(video, null); // Channel not found, but we have video
                }
                Channel channel = channelResponse.getItems().get(0);

                return new VideoDetails(video, channel);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(VideoDetails result) {
            if (result != null && result.video != null) {
                updateUi(result);
            } else {
                Toast.makeText(VideoStatistics.this, "Failed to fetch video details.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void updateUi(VideoDetails details) {
        Video video = details.video;

        // --- Update Statistics Tab ---
        Picasso.get().load(video.getSnippet().getThumbnails().getHigh().getUrl()).into((ImageView)findViewById(R.id.video_thumbnail));
        ((TextView)findViewById(R.id.video_title)).setText(video.getSnippet().getTitle());
        ((TextView)findViewById(R.id.publish_date)).setText(formatDate(video.getSnippet().getPublishedAt().toString()));

        // Main Stats
        setupStatItem(findViewById(R.id.views_stat), R.drawable.view, "Views", formatCount(video.getStatistics().getViewCount()));
        setupStatItem(findViewById(R.id.likes_stat), R.drawable.like, "Likes", formatCount(video.getStatistics().getLikeCount()));
        setupStatItem(findViewById(R.id.comments_stat), R.drawable.comment, "Comments", formatCount(video.getStatistics().getCommentCount()));

        // Ratios
        BigDecimal views = new BigDecimal(video.getStatistics().getViewCount());
        BigDecimal likes = new BigDecimal(video.getStatistics().getLikeCount());
        BigDecimal comments = new BigDecimal(video.getStatistics().getCommentCount());

        setupRatioItem(findViewById(R.id.like_per_view_item), "Like-to-View Ratio", calculateRatio(likes, views));
        setupRatioItem(findViewById(R.id.comment_per_view_item), "Comment-to-View Ratio", calculateRatio(comments, views));
        setupRatioItem(findViewById(R.id.engagement_rate_item), "Engagement Rate", calculateEngagementRate(likes, comments, views));

        // --- Update Analysis Tab ---
        setupLikeCommentPieChart(likes.intValue(), comments.intValue());

        // --- Update Rank Test Tab ---
        if (details.channel != null) {
            performRankTest(video, details.channel);
        } else {
            findViewById(R.id.rank_test).setVisibility(View.GONE); // Hide tab if no channel data
            Toast.makeText(this, "Could not fetch channel data for rank test.", Toast.LENGTH_SHORT).show();
        }
    }

    private void performRankTest(Video video, Channel channel) {
        // Safe conversion from BigInteger to double for calculations
        double videoViews = video.getStatistics().getViewCount() != null ? video.getStatistics().getViewCount().doubleValue() : 0.0;
        double videoLikes = video.getStatistics().getLikeCount() != null ? video.getStatistics().getLikeCount().doubleValue() : 0.0;
        double videoComments = video.getStatistics().getCommentCount() != null ? video.getStatistics().getCommentCount().doubleValue() : 0.0;
        
        long channelSubs = channel.getStatistics().getSubscriberCount() != null ? channel.getStatistics().getSubscriberCount().longValue() : 1L; // Avoid division by zero
        long channelTotalViews = channel.getStatistics().getViewCount() != null ? channel.getStatistics().getViewCount().longValue() : 1L;
        long channelVideoCount = channel.getStatistics().getVideoCount() != null ? channel.getStatistics().getVideoCount().longValue() : 1L;
        
        // Prevent division by zero
        if (channelVideoCount == 0) channelVideoCount = 1;
        if (channelSubs == 0) channelSubs = 1;

        double channelAvgViews = (double) channelTotalViews / channelVideoCount;

        // Test 1: Avg View Test
        double test1Score = (videoViews >= channelAvgViews) ? 20.0 : (videoViews / channelAvgViews) * 20.0;
        
        // Test 2: Like Test (benchmark: 4% likes per view)
        double likeBenchmark = videoViews * 0.04;
        double test2Score = (videoLikes >= likeBenchmark) ? 20.0 : (videoLikes / (likeBenchmark > 0 ? likeBenchmark : 1)) * 20.0;
        
        // Test 3: Comment Test (benchmark: 0.2% comments per view)
        double commentBenchmark = videoViews * 0.002;
        double test3Score = (videoComments >= commentBenchmark) ? 20.0 : (videoComments / (commentBenchmark > 0 ? commentBenchmark : 1)) * 20.0;
        
        // Test 4: Sub View Test
        double test4Score = (videoViews >= channelSubs) ? 20.0 : (videoViews / channelSubs) * 20.0;
        
        // Test 5: Engagement Rate Test (benchmark: 5%)
        double engagementRate = Double.parseDouble(calculateEngagementRate(new BigDecimal(videoLikes), new BigDecimal(videoComments), new BigDecimal(videoViews)).replace("%", ""));
        double test5Score = (engagementRate >= 5.0) ? 20.0 : (engagementRate / 5.0) * 20.0;

        double grandTotal = Math.min(100.0, test1Score + test2Score + test3Score + test4Score + test5Score); // Cap at 100

        // Update UI
        updateRankTestUI(test1Score, test2Score, test3Score, test4Score, test5Score, grandTotal);
    }
    
    private void updateRankTestUI(double s1, double s2, double s3, double s4, double s5, double total) {
        setupRatioItem(findViewById(R.id.avg_view_test_item), "Average View Test", formatScore(s1));
        setupRatioItem(findViewById(R.id.like_test_item), "Like Ratio Test", formatScore(s2));
        setupRatioItem(findViewById(R.id.comment_test_item), "Comment Ratio Test", formatScore(s3));
        setupRatioItem(findViewById(R.id.sub_view_test_item), "Subscriber to View Test", formatScore(s4));
        setupRatioItem(findViewById(R.id.er_test_item), "Engagement Rate Test", formatScore(s5));
        setupRatioItem(findViewById(R.id.total_item), "Grand Total", String.format(Locale.US, "%.1f / 100", total));

        int totalInt = (int) Math.round(total);
        ProgressBar progressCircle = findViewById(R.id.progressCircle);
        progressCircle.setProgress(totalInt);

        TextView percentageText = findViewById(R.id.Percentage);
        percentageText.setText(totalInt + "%");
        
        TextView gradeText = findViewById(R.id.Grade);
        String grade;
        int gradeColor;

        if (total >= 90) { grade = "A+"; gradeColor = Color.parseColor("#2E7D32"); } 
        else if (total >= 80) { grade = "A"; gradeColor = Color.parseColor("#4CAF50"); }
        else if (total >= 70) { grade = "B"; gradeColor = Color.parseColor("#8BC34A"); }
        else if (total >= 60) { grade = "C"; gradeColor = Color.parseColor("#FDD835"); }
        else if (total >= 50) { grade = "D"; gradeColor = Color.parseColor("#FF9800"); }
        else { grade = "F"; gradeColor = Color.parseColor("#D32F2F"); }

        gradeText.setText(grade);
        gradeText.setTextColor(gradeColor);
        percentageText.setTextColor(gradeColor);
    }

    // --- UI Helper Methods ---
    private void setupStatItem(View statView, int iconRes, String label, String value) {
        ((ImageView) statView.findViewById(R.id.stat_icon)).setImageResource(iconRes);
        ((TextView) statView.findViewById(R.id.stat_label)).setText(label);
        ((TextView) statView.findViewById(R.id.stat_value)).setText(value);
    }

    private void setupRatioItem(View ratioView, String label, String value) {
        ((TextView) ratioView.findViewById(R.id.ratio_label)).setText(label);
        TextView valueView = ratioView.findViewById(R.id.ratio_value);
        valueView.setText(value);
    }
    
    private String formatScore(double score) {
        return String.format(Locale.US, "%.1f / 20", Math.min(20.0, score)); // Cap score at 20
    }

    // --- Calculation & Formatting Helpers ---
    private String calculateRatio(BigDecimal numerator, BigDecimal denominator) {
        if (denominator.compareTo(BigDecimal.ZERO) == 0) return "0.00%";
        BigDecimal ratio = numerator.divide(denominator, 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
        return String.format(Locale.US, "%.2f%%", ratio);
    }
    
    private String calculateEngagementRate(BigDecimal likes, BigDecimal comments, BigDecimal views) {
        if (views.compareTo(BigDecimal.ZERO) == 0) return "0.00%";
        BigDecimal engagement = likes.add(comments).divide(views, 4, RoundingMode.HALF_UP).multiply(new BigDecimal(100));
        return String.format(Locale.US, "%.2f%%", engagement);
    }

    public static String formatCount(BigInteger count) {
        if (count == null) return "0";
        long longCount = count.longValue();
        if (longCount >= 1_000_000_000) return String.format(Locale.US, "%.1fB", longCount / 1_000_000_000.0);
        if (longCount >= 1_000_000) return String.format(Locale.US, "%.1fM", longCount / 1_000_000.0);
        if (longCount >= 1_000) return String.format(Locale.US, "%.1fK", longCount / 1_000.0);
        return String.valueOf(longCount);
    }
    
    public String formatDate(String utcDate) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
            inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = inputFormat.parse(utcDate);
            SimpleDateFormat outputFormat = new SimpleDateFormat("d MMMM yyyy", Locale.getDefault());
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return "Unknown date";
        }
    }

    // --- Charting ---
    private void setupLikeCommentPieChart(int likes, int comments) {
        AnyChartView anyChartView = findViewById(R.id.chart1);
        anyChartView.setProgressBar(findViewById(R.id.progressBar)); // Assuming you have a progress bar for charts
        Pie pie = AnyChart.pie();
        List<DataEntry> data = new ArrayList<>();
        data.add(new ValueDataEntry("Likes", likes));
        data.add(new ValueDataEntry("Comments", comments));
        pie.data(data);
        pie.labels().position("outside");
        pie.legend().title().enabled(false);
        pie.legend().position("center-bottom").itemsLayout(LegendLayout.HORIZONTAL).align(Align.CENTER);
        pie.background().enabled(false);
        anyChartView.setChart(pie);
    }

    // --- YouTube API Service ---
    public static YouTube getYouTubeService(String apiKey) {
        try {
            return new YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, null)
                    .setApplicationName(APPLICATION_NAME)
                    .setYouTubeRequestInitializer(new YouTubeRequestInitializer(apiKey))
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}