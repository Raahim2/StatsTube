package com.example.stattube;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge; // <-- IMPORT ADDED
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets; // <-- IMPORT ADDED
import androidx.core.view.ViewCompat; // <-- IMPORT ADDED
import androidx.core.view.WindowInsetsCompat; // <-- IMPORT ADDED
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.YouTubeRequestInitializer;
import com.google.api.services.youtube.model.Channel;
import com.google.api.services.youtube.model.ChannelListResponse;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoListResponse;

import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private enum SearchType { CHANNEL, VIDEO }
    private SearchType currentSearchType = SearchType.CHANNEL;

    private TextInputEditText queryEditText;
    private RecyclerView channelsRecyclerView;
    private RecyclerView videosRecyclerView;
    private ProgressBar progressBar;
    private TextView emptyStateTextView;
    private MaterialButtonToggleGroup toggleGroup;

    private ChannelAdapter channelAdapter;
    private VideoAdapter videoAdapter;

    private static final String APPLICATION_NAME = "Statstube";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    public static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static final String API_KEY = BuildConfig.YOUTUBE_API_KEY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this); // This tells the app to draw behind the system bars.
        setContentView(R.layout.activity_main);

        // This listener gets the size of the system bars and applies it as padding
        // to the main layout, preventing your content from being hidden underneath.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Views
        queryEditText = findViewById(R.id.query);
        channelsRecyclerView = findViewById(R.id.channelsRecyclerView);
        videosRecyclerView = findViewById(R.id.videosRecyclerView);
        progressBar = findViewById(R.id.progressBar);
        emptyStateTextView = findViewById(R.id.emptyStateTextView);
        toggleGroup = findViewById(R.id.toggle_button_group);

        setupRecyclerViews();
        setupSearchTypeToggle();
        setupSearchInput();
    }

    private void setupRecyclerViews() {
        channelsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        videosRecyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void setupSearchInput() {
        queryEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                Search(v);
                return true;
            }
            return false;
        });
    }

    private void setupSearchTypeToggle() {
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.channel_toggle_button) {
                    currentSearchType = SearchType.CHANNEL;
                    channelsRecyclerView.setVisibility(View.VISIBLE);
                    videosRecyclerView.setVisibility(View.GONE);
                } else if (checkedId == R.id.video_toggle_button) {
                    currentSearchType = SearchType.VIDEO;
                    channelsRecyclerView.setVisibility(View.GONE);
                    videosRecyclerView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void Search(View v) {
        String query = queryEditText.getText().toString().trim();
        if (query.isEmpty()) {
            Toast.makeText(this, "Please enter a search query", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentSearchType == SearchType.CHANNEL) {
            new GetChannelsTask().execute(query);
        } else {
            new GetVideosTask().execute(query);
        }
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            emptyStateTextView.setVisibility(View.GONE);
            channelsRecyclerView.setVisibility(View.GONE);
            videosRecyclerView.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void showEmptyState(boolean show) {
        if(show){
            emptyStateTextView.setVisibility(View.VISIBLE);
            channelsRecyclerView.setVisibility(View.GONE);
            videosRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateTextView.setVisibility(View.GONE);
        }
    }


    // --- AsyncTasks ---

    public class GetChannelsTask extends AsyncTask<String, Void, List<ChannelInfo>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showLoading(true);
        }

        @Override
        protected List<ChannelInfo> doInBackground(String... params) {
            String query = params[0];
            List<ChannelInfo> resultList = new ArrayList<>();
            YouTube youtubeService = getYouTubeService(API_KEY);
            if (youtubeService == null) return resultList;

            try {
                SearchListResponse searchResponse = youtubeService.search().list("snippet")
                        .setQ(query)
                        .setType("channel")
                        .setMaxResults(10L)
                        .execute();

                List<SearchResult> searchResults = searchResponse.getItems();
                if (searchResults.isEmpty()) return resultList;

                List<String> channelIds = new ArrayList<>();
                for(SearchResult result : searchResults){
                    channelIds.add(result.getId().getChannelId());
                }

                ChannelListResponse channelsResponse = youtubeService.channels().list("snippet,statistics")
                        .setId(String.join(",", channelIds))
                        .execute();
                List<Channel> channels = channelsResponse.getItems();

                for (Channel channel : channels) {
                    resultList.add(new ChannelInfo(
                            channel.getSnippet().getTitle(),
                            channel.getId(),
                            channel.getStatistics().getSubscriberCount(),
                            channel.getSnippet().getThumbnails().getDefault().getUrl()
                    ));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return resultList;
        }

        @Override
        protected void onPostExecute(List<ChannelInfo> result) {
            showLoading(false);
            if (result == null || result.isEmpty()){
                showEmptyState(true);
            } else {
                showEmptyState(false);
                channelAdapter = new ChannelAdapter(MainActivity.this, result);
                channelsRecyclerView.setAdapter(channelAdapter);
                channelsRecyclerView.setVisibility(View.VISIBLE);
            }
        }
    }

    public class GetVideosTask extends AsyncTask<String, Void, List<VideoInfo>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showLoading(true);
        }

        @Override
        protected List<VideoInfo> doInBackground(String... params) {
            String query = params[0];
            List<VideoInfo> resultList = new ArrayList<>();
            YouTube youtubeService = getYouTubeService(API_KEY);
            if (youtubeService == null) return resultList;

            try {
                SearchListResponse searchResponse = youtubeService.search().list("snippet")
                        .setQ(query)
                        .setType("video")
                        .setMaxResults(10L)
                        .execute();

                List<SearchResult> searchResults = searchResponse.getItems();
                if (searchResults.isEmpty()) return resultList;

                List<String> videoIds = new ArrayList<>();
                for(SearchResult result : searchResults){
                    videoIds.add(result.getId().getVideoId());
                }

                VideoListResponse videosResponse = youtubeService.videos().list("snippet,statistics")
                        .setId(String.join(",", videoIds))
                        .execute();
                List<Video> videos = videosResponse.getItems();

                for (Video video : videos) {
                    resultList.add(new VideoInfo(
                            video.getSnippet().getTitle(),
                            video.getStatistics().getViewCount(),
                            video.getStatistics().getLikeCount(),
                            video.getStatistics().getCommentCount(),
                            video.getSnippet().getPublishedAt().toString(),
                            video.getSnippet().getThumbnails().getHigh().getUrl(),
                            video.getId()
                    ));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return resultList;
        }

        @Override
        protected void onPostExecute(List<VideoInfo> result) {
            showLoading(false);
            if (result == null || result.isEmpty()){
                showEmptyState(true);
            } else {
                showEmptyState(false);
                videoAdapter = new VideoAdapter(result, videoId -> {
                    Intent intent = new Intent(MainActivity.this, VideoStatistics.class);
                    intent.putExtra("VIDEO_ID", videoId);
                    startActivity(intent);
                });
                videosRecyclerView.setAdapter(videoAdapter);
                videosRecyclerView.setVisibility(View.VISIBLE);
            }
        }
    }

    public static YouTube getYouTubeService(String apiKey) {
        try {
            return new YouTube.Builder(GoogleNetHttpTransport.newTrustedTransport(), JSON_FACTORY, null)
                    .setApplicationName(APPLICATION_NAME)
                    .setYouTubeRequestInitializer(new YouTubeRequestInitializer(apiKey))
                    .build();
        } catch (GeneralSecurityException | IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String formatCount(BigInteger count) {
        if (count == null) return "0";
        try {
            long longCount = count.longValue();
            if (longCount >= 1_000_000_000) {
                return String.format(Locale.US, "%.1fB", longCount / 1_000_000_000.0);
            } else if (longCount >= 1_000_000) {
                return String.format(Locale.US, "%.1fM", longCount / 1_000_000.0);
            } else if (longCount >= 1_000) {
                return String.format(Locale.US, "%.1fK", longCount / 1_000.0);
            } else {
                return String.valueOf(longCount);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return "N/A";
        }
    }
}