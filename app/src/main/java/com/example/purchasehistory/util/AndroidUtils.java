package com.example.purchasehistory.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.util.Patterns;
import com.example.purchasehistory.MainActivity;
import com.example.purchasehistory.PurchaseHistoryApplication;

public final class AndroidUtils {
    public static void shareString(String token, String title, Context context) {
        Log.i("Sharing", "Attempting to share a string.");
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, token);
        sendIntent.putExtra(Intent.EXTRA_TITLE, title);

        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        context.startActivity(shareIntent);
    }

    public static void logout(Context context) {
        PurchaseHistoryApplication.getInstance().userToken.postValue(null);
        PurchaseHistoryApplication.getInstance().loggedUser.postValue(null);
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    public static int getTextColor(int bgColor) {
        if (Color.luminance(bgColor) > 0.5)
            return Color.BLACK;
        else
            return Color.WHITE;
    }

    // A placeholder username validation check
    public static boolean isUserNameValid(String username) {
        if (username == null || username.trim().length() <= 5) {
            return false;
        } else {
            return !username.trim().isEmpty();
        }
    }

    // A placeholder email validation check
    public static boolean isEmailValid(String email) {
        if (email == null) {
            return false;
        } else return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }


    // A placeholder password validation check
    public static boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }
}
