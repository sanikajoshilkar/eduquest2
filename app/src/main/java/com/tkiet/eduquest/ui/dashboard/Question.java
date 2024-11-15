package com.tkiet.eduquest.ui.dashboard;

public class Question {
    private String questionText;
    private String userName;
    private String userPhotoUrl;

    public Question(String questionText, String userName, String userPhotoUrl) {
        this.questionText = questionText;
        this.userName = userName;
        this.userPhotoUrl = userPhotoUrl;
    }

    public String getQuestionText() {
        return questionText;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserPhotoUrl() {
        return userPhotoUrl;
    }
}
