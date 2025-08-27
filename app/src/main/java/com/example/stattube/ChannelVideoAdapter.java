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
import java.util.List;

public class ChannelVideoAdapter extends RecyclerView.Adapter<ChannelVideoAdapter.VideoViewHolder> {

    private final List<Video> videos;
    private final Context context;

    public ChannelVideoAdapter(Context context, List<Video> videos) {
        this.context = context;
        this.videos = videos;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        Video video = videos.get(position);
        holder.bind(video);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, VideoStatistics.class);
            intent.putExtra("VIDEO_ID", video.getId());
            context.startActivity(intent);
        });
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
}