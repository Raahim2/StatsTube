package com.example.stattube;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.api.services.youtube.model.Video;
import com.squareup.picasso.Picasso;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class AllVideosAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_LOADING = 1;

    private final Context context;
    private final List<Video> videos = new ArrayList<>();
    private boolean isLoading = false;

    public AllVideosAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_video, parent, false);
            return new VideoViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_loading, parent, false);
            return new LoadingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof VideoViewHolder) {
            Video video = videos.get(position);
            ((VideoViewHolder) holder).bind(video);
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(context, VideoStatistics.class);
                intent.putExtra("VIDEO_ID", video.getId());
                context.startActivity(intent);
            });
        }
    }

    @Override
    public int getItemCount() {
        return videos.size() + (isLoading ? 1 : 0);
    }

    @Override
    public int getItemViewType(int position) {
        if (isLoading && position == videos.size()) {
            return VIEW_TYPE_LOADING;
        }
        return VIEW_TYPE_ITEM;
    }

    public void addVideos(List<Video> newVideos) {
        int startPosition = videos.size();
        videos.addAll(newVideos);
        notifyItemRangeInserted(startPosition, newVideos.size());
    }

    public void setLoading(boolean loading) {
        if (isLoading == loading) return;
        isLoading = loading;
        if (isLoading) {
            notifyItemInserted(videos.size());
        } else {
            notifyItemRemoved(videos.size());
        }
    }

    // --- ViewHolders ---

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        TextView title;
        TextView stats;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.video_thumbnail);
            title = itemView.findViewById(R.id.video_title);
            stats = itemView.findViewById(R.id.video_stats);
        }

        void bind(final Video video) {
            title.setText(video.getSnippet().getTitle());
            BigInteger viewCount = video.getStatistics().getViewCount();
            BigInteger likeCount = video.getStatistics().getLikeCount();
            String videoStats = String.format("%s views • %s likes",
                    ChannelStastics.formatCount(viewCount != null ? viewCount : BigInteger.ZERO),
                    ChannelStastics.formatCount(likeCount != null ? likeCount : BigInteger.ZERO));
            stats.setText(videoStats);
            Picasso.get().load(video.getSnippet().getThumbnails().getHigh().getUrl()).placeholder(R.drawable.test).into(thumbnail);
        }
    }

    static class LoadingViewHolder extends RecyclerView.ViewHolder {
        public LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}