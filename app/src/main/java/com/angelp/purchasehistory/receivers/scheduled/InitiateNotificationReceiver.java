package com.angelp.purchasehistory.receivers.scheduled;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import com.angelp.purchasehistory.data.Constants;
import com.angelp.purchasehistory.data.model.ScheduledNotification;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class InitiateNotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "InitiateNotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        ArrayList<ScheduledNotification> scheduledExpenses = intent.getParcelableArrayListExtra(Constants.Arguments.NOTIFICATION_EXTRA_ARG);
        if (scheduledExpenses == null) {
            Log.e(TAG, "Invalid list");
            return;
        }
        if (scheduledExpenses.isEmpty()) {
            Log.i(TAG, "There are no scheduled expenses to notify about");
            return;
        }
        updateAlarms(context, scheduledExpenses);

    }

    private void updateAlarms(Context context, List<ScheduledNotification> scheduledExpenses) {
        if (context == null) {
            return;
        }
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        SharedPreferences preferences = context.getSharedPreferences(Constants.Preferences.SILENCED_NOTIFICATIONS, MODE_PRIVATE);


        for (ScheduledNotification notification : scheduledExpenses) {
            boolean isSilenced = preferences.getBoolean(notification.getId().toString(), false);
            Intent myIntent = new Intent(context, ScheduledNotificationReceiver.class);
            myIntent.putExtra(ScheduledNotificationReceiver.SCHEDULED_NOTIFICATION_EXTRA, notification);
            myIntent.setIdentifier("Notification_" + notification.getId());
            scheduleNotification(notification, context, myIntent, alarmManager, notification.getEnabled() && !isSilenced);
        }
    }

    private void scheduleNotification(ScheduledNotification scheduledNotification, Context context, Intent myIntent, AlarmManager alarmManager, boolean isActive) {
        int requestId = Math.toIntExact(scheduledNotification.getId());
        if (isActive) {
            Log.i(TAG, "scheduleNotification: " + scheduledNotification.getNote());
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestId, myIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_CANCEL_CURRENT);
            if (scheduledNotification.isRepeating())
                alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, scheduledNotification.getTimestamp(), scheduledNotification.getPeriod(), pendingIntent);
            else alarmManager.set(AlarmManager.RTC_WAKEUP, scheduledNotification.getTimestamp(), pendingIntent);
        } else {
            PendingIntent pendingIntent = PendingIntent.getService(context, requestId, myIntent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent);
                Log.i(TAG, "Canceled scheduled notification for " + scheduledNotification.getNote());
            }
        }

    }
}
