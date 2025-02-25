package com.angelp.purchasehistory;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

public class GlobalExceptionHandler implements Thread.UncaughtExceptionHandler {
    private final Context context;
    private final Thread.UncaughtExceptionHandler defaultHandler;

    public GlobalExceptionHandler(Context context) {
        this.context = context;
        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable throwable) {
        Log.e("GlobalExceptionHandler", "Uncaught exception. Attempting to display feedback screen" + throwable.getClass().getSimpleName());
        new Handler(Looper.getMainLooper()).post(() -> {
            Intent intent = new Intent(context, ErrorFallbackActivity.class);
            intent.putExtra("error_details", Log.getStackTraceString(throwable));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(intent);
        });
        // Allow some time for the error activity to start before killing the app
//        new Handler().postDelayed(() -> defaultHandler.uncaughtException(thread, throwable), 2000);
    }
}