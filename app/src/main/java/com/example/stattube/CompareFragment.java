package com.example.stattube;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
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
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class CompareFragment extends Fragment implements ChannelSearchAdapter.OnChannelClickListener {

    private TextInputEditText searchEditText;
    private RecyclerView resultsRecyclerView;
    private ProgressBar searchProgressBar;
    private CardView selectedChannelCard;
    private Button compareButton;
    private String selectedChannelId;
    private DeepAnalysis.ChannelData originalChannelData;
    
    // Static YouTube API constants
    private static final String APPLICATION_NAME = "Statstube";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();
    private static final String API_KEY = BuildConfig.YOUTUBE_API_KEY;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_compare, container, false);

        searchEditText = view.findViewById(R.id.search_edit_text);
        resultsRecyclerView = view.findViewById(R.id.results_recycler_view);
        searchProgressBar = view.findViewById(R.id.search_progress_bar);
        selectedChannelCard = view.findViewById(R.id.selected_channel_card);
        compareButton = view.findViewById(R.id.compare_button);

        resultsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        ChannelViewModel viewModel = new ViewModelProvider(requireActivity()).get(ChannelViewModel.class);
        viewModel.getChannelData().observe(getViewLifecycleOwner(), data -> this.originalChannelData = data);

        setupSearch();

        compareButton.setOnClickListener(v -> {
            if (originalChannelData != null && selectedChannelId != null) {
                if (originalChannelData.id.equals(selectedChannelId)) {
                    Toast.makeText(getContext(), "You cannot compare a channel with itself.", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent intent = new Intent(getActivity(), CompairChannel.class);
                intent.putExtra("CHANNEL1_ID", originalChannelData.id);
                intent.putExtra("CHANNEL2_ID", selectedChannelId);
                startActivity(intent);
            }
        });

        return view;
    }

    private void setupSearch() {
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                String query = searchEditText.getText().toString().trim();
                if (!query.isEmpty()) {
                    hideKeyboard();
                    new SearchChannelsTask().execute(query);
                }
                return true;
            }
            return false;
        });
    }

    @Override
    public void onChannelClick(ChannelInfo channelInfo) {
        selectedChannelId = channelInfo.getChannelId();
        selectedChannelCard.setVisibility(View.VISIBLE);
        resultsRecyclerView.setVisibility(View.GONE);

        // Populate the selected channel card
        View itemView = selectedChannelCard.findViewById(R.id.selected_channel_item);
        ((TextView) itemView.findViewById(R.id.channel_title)).setText(channelInfo.getTitle());
        String subs = DeepAnalysis.formatCount(channelInfo.getSubscriberCount().toString()) + " Subscribers";
        ((TextView) itemView.findViewById(R.id.channel_subs)).setText(subs);
        Picasso.get().load(channelInfo.getThumbnailUrl()).into((ImageView) itemView.findViewById(R.id.channel_thumbnail));
    }

    private void hideKeyboard() {
        View view = getActivity() != null ? getActivity().getCurrentFocus() : null;
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private class SearchChannelsTask extends AsyncTask<String, Void, List<ChannelInfo>> {
        @Override
        protected void onPreExecute() {
            searchProgressBar.setVisibility(View.VISIBLE);
            resultsRecyclerView.setVisibility(View.GONE);
            selectedChannelCard.setVisibility(View.GONE);
        }

        @Override
        protected List<ChannelInfo> doInBackground(String... params) {
            String query = params[0];
            List<ChannelInfo> resultList = new ArrayList<>();
            YouTube youtubeService = getYouTubeService();
            if (youtubeService == null) return resultList;

            try {
                // Step 1: Search for channel IDs
                SearchListResponse searchResponse = youtubeService.search().list("snippet")
                        .setQ(query)
                        .setType("channel")
                        .setMaxResults(10L)
                        .execute();
                List<SearchResult> searchResults = searchResponse.getItems();
                if (searchResults.isEmpty()) return resultList;

                List<String> channelIds = new ArrayList<>();
                for (SearchResult result : searchResults) {
                    channelIds.add(result.getId().getChannelId());
                }

                // Step 2: Get full details for all found IDs in one call
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
            } catch (IOException e) {
                e.printStackTrace();
            }
            return resultList;
        }

        @Override
        protected void onPostExecute(List<ChannelInfo> result) {
            searchProgressBar.setVisibility(View.GONE);
            if (result != null && !result.isEmpty()) {
                resultsRecyclerView.setVisibility(View.VISIBLE);
                ChannelSearchAdapter adapter = new ChannelSearchAdapter(result, CompareFragment.this);
                resultsRecyclerView.setAdapter(adapter);
            } else {
                Toast.makeText(getContext(), "No channels found", Toast.LENGTH_SHORT).show();
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