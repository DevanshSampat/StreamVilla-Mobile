package com.example.news;

public class NotificationModel {
    private String title;
    private String body;
    private String android_channel_id;
    private String image;

    public void setAndroidChannelId(String android_channel_id) {
        this.android_channel_id = android_channel_id;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public NotificationModel(String title, String body) {
        this.title = title;
        this.body = body;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public void setBody(String body) {
        this.body = body;
    }
}
