package com.example.news;

import com.google.gson.annotations.SerializedName;

import java.util.HashMap;

public class RequestNotification {
    @SerializedName("to") private String token;
    @SerializedName("notification") private NotificationModel notificationModel;
    @SerializedName("data") private HashMap<String,Object> data;

    public void setToken(String token) {
        this.token = token;
    }

    public void setNotificationModel(NotificationModel notificationModel) {
        this.notificationModel = notificationModel;
    }

    public void setData(HashMap<String, Object> data) {
        this.data = data;
    }
}
