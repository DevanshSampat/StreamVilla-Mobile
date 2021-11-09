package com.devansh.entertainment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Settings;
import android.widget.Toast;

import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;

import com.google.firebase.database.DataSnapshot;

public class Utility {

    public static ContentData getContentData(Context context, DataSnapshot snapshot, String dbName) {
        String temporary_name, contentName, contentImage, contentLink, contentDescription;
        int j = 0;
        for (int k = 0; k < dbName.length(); k++) {
            char c = dbName.charAt(k);
            if (c >= '1' && c <= '9') {
                j = k;
                break;
            }
        }
        temporary_name = dbName.substring(0, j);
        contentName = snapshot.child(dbName).getValue().toString();
        contentLink = snapshot.child(temporary_name + "link" + dbName.substring(j)).getValue().toString();
        if (temporary_name.equals("sport"))
            contentImage = snapshot.child("sports_image_link").getValue().toString();
        else
            contentImage = snapshot.child(temporary_name + "image" + dbName.substring(j)).getValue().toString();
        contentDescription = "";
        try {
            contentDescription = snapshot.child(temporary_name + "date" + dbName.substring(j)).getValue().toString();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return new ContentData(context, contentName, contentImage, contentLink, contentDescription,
                "", dbName);

    }

    public static boolean checkForBiometrics(Context context) {
        BiometricManager biometricManager = BiometricManager.from(context);
        switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE: {
            }
            case BiometricManager.BIOMETRIC_STATUS_UNKNOWN:{
            }
            case BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED: {
            }
            case BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED:{
            }
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE: {
                Toast.makeText(context, "Private features cannot be accessed without biometrics", Toast.LENGTH_SHORT).show();
                return false;
            }
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:{
                Toast.makeText(context, "Enroll biometrics before accessing private feature", Toast.LENGTH_SHORT).show();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    Intent intent = new Intent(Settings.ACTION_BIOMETRIC_ENROLL);
                    intent.putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED, BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.DEVICE_CREDENTIAL);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    ((Activity)context).startActivityForResult(intent,2070);
                }
                return false;
            }
        }
        return true;
    }
}
