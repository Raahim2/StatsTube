package com.example.stattube;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {

    private final List<VideoInfo> videos;
    private final OnVideoClickListener listener;
    
    public interface OnVideoClickListener {
        void onVideoClick(String videoId);
    }

    public VideoAdapter(List<VideoInfo> videos, OnVideoClickListener listener) {
        this.videos = videos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        VideoInfo video = videos.get(position);
        holder.bind(video, listener);
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

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

        void bind(final VideoInfo video, final OnVideoClickListener listener) {
            title.setText(video.getTitle());
            String videoStats = String.format("%s views • %s likes",
                    MainActivity.formatCount(video.getViewCount()),
                    MainActivity.formatCount(video.getLikeCount()));
            stats.setText(videoStats);
            
            Picasso.get().load(video.getThumbnailUrl()).placeholder(R.drawable.test).into(thumbnail);
            
            itemView.setOnClickListener(v -> listener.onVideoClick(video.getVideoId()));
        }
    }
}