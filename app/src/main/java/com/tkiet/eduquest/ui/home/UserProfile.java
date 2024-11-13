package com.tkiet.eduquest.ui.home;

public class UserProfile {
    private String userId;    // This is optional if you want to store the user ID separately
    private String imageUrl;
    private String name;
    private String phone;
    private String skills;
    private int likes;

    // Required no-argument constructor for Firebase
    public UserProfile() {
    }

    // Optional constructor for creating UserProfile objects
    public UserProfile(String userId, String imageUrl, String name, String phone, String skills, int likes) {
        this.userId = userId;
        this.imageUrl = imageUrl;
        this.name = name;
        this.phone = phone;
        this.skills = skills;
        this.likes = likes;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSkills() {
        return skills;
    }

    public void setSkills(String skills) {
        this.skills = skills;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }
}
