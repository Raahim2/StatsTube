package com.example.stattube;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import java.util.List;

public class ChannelSearchAdapter extends RecyclerView.Adapter<ChannelSearchAdapter.ChannelViewHolder> {

    public interface OnChannelClickListener {
        void onChannelClick(ChannelInfo channelInfo);
    }

    private final List<ChannelInfo> channels;
    private final OnChannelClickListener listener;

    public ChannelSearchAdapter(List<ChannelInfo> channels, OnChannelClickListener listener) {
        this.channels = channels;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChannelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_channel, parent, false);
        return new ChannelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChannelViewHolder holder, int position) {
        ChannelInfo channel = channels.get(position);
        holder.bind(channel);
        holder.itemView.setOnClickListener(v -> listener.onChannelClick(channel));
    }

    @Override
    public int getItemCount() {
        return channels.size();
    }

    static class ChannelViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;
        TextView title;
        TextView subs;

        public ChannelViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnail = itemView.findViewById(R.id.channel_thumbnail);
            title = itemView.findViewById(R.id.channel_title);
            subs = itemView.findViewById(R.id.channel_subs);
        }

        void bind(ChannelInfo channel) {
            title.setText(channel.getTitle());
            String subCount = DeepAnalysis.formatCount(channel.getSubscriberCount().toString()) + " Subscribers";
            subs.setText(subCount);
            Picasso.get().load(channel.getThumbnailUrl()).placeholder(R.drawable.analysis).into(thumbnail);
        }
    }
}