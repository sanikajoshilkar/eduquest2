package com.tkiet.eduquest.ui.home;

public class User {

    private String name;
    private String imageUrl;
    private String phone;
    private String skills;

    public User() { } // Default constructor required for Firebase

    public String getName() { return name; }
    public String getImageUrl() { return imageUrl; }
    public String getPhone() { return phone; }
    public String getSkills() { return skills; }
}
