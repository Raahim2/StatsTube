package com.example.stattube;

import java.math.BigInteger;

public class ChannelInfo {
    private final String title;
    private final String channelId;
    private final BigInteger subscriberCount;
    private final String thumbnailUrl;

    public ChannelInfo(String title, String channelId, BigInteger subscriberCount, String thumbnailUrl) {
        this.title = title;
        this.channelId = channelId;
        this.subscriberCount = subscriberCount;
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getTitle() { return title; }
    public String getChannelId() { return channelId; }
    public BigInteger getSubscriberCount() { return subscriberCount; }
    public String getThumbnailUrl() { return thumbnailUrl; }
}