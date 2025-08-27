package com.example.stattube;

import java.math.BigInteger;

public class VideoInfo {
    private final String title;
    private final BigInteger viewCount;
    private final BigInteger likeCount;
    private final BigInteger commentCount;
    private final String publishedAt;
    private final String thumbnailUrl;
    private final String videoId;

    public VideoInfo(String title, BigInteger viewCount, BigInteger likeCount, BigInteger commentCount, String publishedAt, String thumbnailUrl, String videoId) {
        this.title = title;
        this.viewCount = viewCount;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.publishedAt = publishedAt;
        this.thumbnailUrl = thumbnailUrl;
        this.videoId = videoId;
    }
    
    // Add getters for all fields
    public String getTitle() { return title; }
    public BigInteger getViewCount() { return viewCount; }
    public BigInteger getLikeCount() { return likeCount; }
    public BigInteger getCommentCount() { return commentCount; }
    public String getPublishedAt() { return publishedAt; }
    public String getThumbnailUrl() { return thumbnailUrl; }
    public String getVideoId() { return videoId; }
}