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
import com.squareup.picasso.Picasso;
import java.util.List;

public class ChannelAdapter extends RecyclerView.Adapter<ChannelAdapter.ChannelViewHolder> {

    private final List<ChannelInfo> channels;
    private final Context context; // Add context

    public ChannelAdapter(Context context, List<ChannelInfo> channels) { // Modify constructor
        this.context = context;
        this.channels = channels;
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

        // Set the click listener here
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChannelStastics.class);
            intent.putExtra("CHANNEL_ID", channel.getChannelId());
            intent.putExtra("CHANNEL_NAME", channel.getTitle());
            context.startActivity(intent);
        });
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
            String subCount = MainActivity.formatCount(channel.getSubscriberCount()) + " Subscribers";
            subs.setText(subCount);
            Picasso.get().load(channel.getThumbnailUrl()).placeholder(R.drawable.analysis).into(thumbnail);
        }
    }
}