package com.tkiet.eduquest.ui.account;

public class VideoModel {
    private String videoId;
    private String title;
    private String description;
    private String tags;
    private String videoUrl;
    private String thumbnailUrl;

    // Constructor (empty for Firebase)
    public VideoModel() {}

    // Constructor
    public VideoModel(String videoId, String title, String description, String tags, String videoUrl, String thumbnailUrl) {
        this.videoId = videoId;
        this.title = title;
        this.description = description;
        this.tags = tags;
        this.videoUrl = videoUrl;
        this.thumbnailUrl = thumbnailUrl;
    }

    // Getter and Setter methods
    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
}
