package com.example.stattube;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AllVideos extends AppCompatActivity {

    private static class VideoPage {
        final List<Video> videos;
        final String nextPageToken;

        VideoPage(List<Video> videos, String nextPageToken) {
            this.videos = videos;
            this.nextPageToken = nextPageToken;
        }
    }

    private static final String APPLICATION_NAME = "Statstube";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static final String API_KEY = BuildConfig.YOUTUBE_API_KEY;

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private AllVideosAdapter adapter;
    private LinearLayoutManager layoutManager;

    private String uploadsPlaylistId;
    private String nextPageToken = null;
    private boolean isLoading = false;
    private boolean isLastPage = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_videos);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        progressBar = findViewById(R.id.progress_bar);
        recyclerView = findViewById(R.id.videos_recycler_view);
        
        Intent intent = getIntent();
        String channelId = intent.getStringExtra("CHANNEL_ID");
        String channelName = intent.getStringExtra("CHANNEL_NAME");
        getSupportActionBar().setTitle(channelName != null ? channelName : "All Videos");

        setupRecyclerView();
        
        if (channelId != null) {
            new FetchPlaylistIdTask().execute(channelId);
        } else {
            Toast.makeText(this, "Channel ID missing", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupRecyclerView() {
        adapter = new AllVideosAdapter(this);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                int visibleItemCount = layoutManager.getChildCount();
                int totalItemCount = layoutManager.getItemCount();
                int firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition();

                if (!isLoading && !isLastPage) {
                    if ((visibleItemCount + firstVisibleItemPosition) >= totalItemCount
                            && firstVisibleItemPosition >= 0
                            && totalItemCount >= 20) { // Fetch next page if we have at least one full page
                        fetchVideos();
                    }
                }
            }
        });
    }

    private void fetchVideos() {
        if (uploadsPlaylistId == null) return;
        isLoading = true;
        if (adapter.getItemCount() > 0) {
            adapter.setLoading(true);
        } else {
            progressBar.setVisibility(View.VISIBLE);
        }
        new FetchVideosTask().execute(uploadsPlaylistId, nextPageToken);
    }
    
    private class FetchPlaylistIdTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                YouTube youtube = getYouTubeService();
                ChannelListResponse response = youtube.channels().list("contentDetails").setId(params[0]).execute();
                if (!response.getItems().isEmpty()) {
                    return response.getItems().get(0).getContentDetails().getRelatedPlaylists().getUploads();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String playlistId) {
            if (playlistId != null) {
                uploadsPlaylistId = playlistId;
                fetchVideos(); // Fetch the first page
            } else {
                Toast.makeText(AllVideos.this, "Could not find channel's uploads", Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        }
    }


    private class FetchVideosTask extends AsyncTask<String, Void, VideoPage> {
        @Override
        protected VideoPage doInBackground(String... params) {
            String playlistId = params[0];
            String token = params[1];
            YouTube youtube = getYouTubeService();

            try {
                // Step 1: Get video IDs and next page token from the playlist
                PlaylistItemListResponse playlistResponse = youtube.playlistItems()
                        .list("contentDetails")
                        .setPlaylistId(playlistId)
                        .setMaxResults(20L) // Fetch 20 items per page
                        .setPageToken(token)
                        .execute();

                List<String> videoIds = new ArrayList<>();
                for (PlaylistItem item : playlistResponse.getItems()) {
                    videoIds.add(item.getContentDetails().getVideoId());
                }

                if (videoIds.isEmpty()) {
                    return new VideoPage(new ArrayList<>(), null);
                }

                // Step 2: Get full video details in one call
                VideoListResponse videoListResponse = youtube.videos()
                        .list("snippet,statistics")
                        .setId(String.join(",", videoIds))
                        .execute();
                
                return new VideoPage(videoListResponse.getItems(), playlistResponse.getNextPageToken());

            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(VideoPage result) {
            progressBar.setVisibility(View.GONE);
            adapter.setLoading(false);
            isLoading = false;

            if (result != null) {
                adapter.addVideos(result.videos);
                nextPageToken = result.nextPageToken;
                if (nextPageToken == null) {
                    isLastPage = true;
                }
            } else {
                Toast.makeText(AllVideos.this, "Error fetching videos", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public static YouTube getYouTubeService() {
        return new YouTube.Builder(HTTP_TRANSPORT, JSON_FACTORY, null)
                .setApplicationName(APPLICATION_NAME)
                .setYouTubeRequestInitializer(new YouTubeRequestInitializer(API_KEY))
                .build();
    }
}