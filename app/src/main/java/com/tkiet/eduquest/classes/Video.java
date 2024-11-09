package com.tkiet.eduquest.classes;

public class Video {
    public String title, description, tags, userId, videoUrl, videoId;

    public Video(String title, String description, String tags, String userId, String videoUrl, String videoId) {
        this.title = title;
        this.description = description;
        this.tags = tags;
        this.userId = userId;
        this.videoUrl = videoUrl;
        this.videoId = videoId;
    }
}

