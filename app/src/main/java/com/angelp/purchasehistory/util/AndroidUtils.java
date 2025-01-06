package com.angelp.purchasehistory.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.util.Log;
import android.util.Patterns;
import com.angelp.purchasehistory.MainActivity;
import com.angelp.purchasehistory.PurchaseHistoryApplication;
import com.angelp.purchasehistorybackend.models.views.outgoing.CategoryView;

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
    public static int getColor(CategoryView category) {
        if (category != null && category.getColor() != null && !category.getColor().isBlank())
            return Color.parseColor(category.getColor());
        else return Color.GRAY;
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

    public static void openCsvFile(Context context, Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "text/csv");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Check if there's an activity available to handle this intent
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        } else {
            // Handle the case when there's no activity available to handle the intent
            PurchaseHistoryApplication.getInstance().alert("No application available to open CSV files");
        }
    }
}
