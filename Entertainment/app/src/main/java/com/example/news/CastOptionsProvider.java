package com.example.news;

import android.content.Context;

import com.google.android.gms.cast.framework.CastOptions;
import com.google.android.gms.cast.framework.OptionsProvider;
import com.google.android.gms.cast.framework.SessionProvider;
import com.google.android.gms.cast.framework.media.CastMediaOptions;
import com.google.android.gms.cast.framework.media.NotificationOptions;

import java.util.ArrayList;
import java.util.List;

public class CastOptionsProvider implements OptionsProvider {
    public static final String CUSTOM_NAMESPACE = "urn:x-cast:custom_namespace";
    @Override
    public CastOptions getCastOptions(Context context) {
        List<String> supportedNamespaces = new ArrayList<>();
        supportedNamespaces.add(CUSTOM_NAMESPACE);
        NotificationOptions notificationOptions = new NotificationOptions.Builder()
                .setTargetActivityClassName(ExpandedControls.class.getName())
                .build();
        CastMediaOptions mediaOptions = new CastMediaOptions.Builder()
                .setNotificationOptions(notificationOptions)
                .setExpandedControllerActivityClassName(ExpandedControls.class.getName())
                .build();
        CastOptions castOptions = new CastOptions.Builder()
                .setReceiverApplicationId("CC1AD845")
                .setCastMediaOptions(mediaOptions)
                .build();
        return castOptions;
    }
    @Override
    public List<SessionProvider> getAdditionalSessionProviders(Context context) {
        return null;
    }

}