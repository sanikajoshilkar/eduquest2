package com.tkiet.eduquest.ui.account;

public class VideoModel {

    private String videoId;
    private String title;
    private String description;
    private String tags;
    private String videoUrl;
    private String thumbnailUrl;
    private String addedBy;
    // Constructor (required for Firebase)
    public VideoModel() {
    }

    public VideoModel(String title, String description, String tags, String videoUrl , String addedBy) {
        this.title = title;
        this.description = description;
        this.tags = tags;
        this.videoUrl = videoUrl;
        this.addedBy=addedBy;
    }
    // Getter and Setter methods for all fields
    public String getAddedBy() {
        return addedBy;
    }

    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }
    // Getter and Setter for videoId
    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    // Getter and Setter for title
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    // Getter and Setter for description
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Getter and Setter for tags
    public String getTags() {
        return tags;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    // Getter and Setter for videoUrl
    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    // Getter and Setter for thumbnailUrl
    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }
}
