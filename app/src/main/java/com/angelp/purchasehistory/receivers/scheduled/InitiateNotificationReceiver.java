package com.angelp.purchasehistory.receivers.scheduled;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.angelp.purchasehistory.data.model.ScheduledNotification;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.NOTIFICATION_SERVICE;

public class InitiateNotificationReceiver extends BroadcastReceiver {
    private static final String TAG = "InitiateNotificationReceiver";
    public static final String NOTIFICATION_EXTRA_ARG = "scheduledNotifications";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Received signal to initiate notification alarms");

        ArrayList<ScheduledNotification> scheduledExpenses = intent.getParcelableArrayListExtra(NOTIFICATION_EXTRA_ARG);
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

        NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel channel = new NotificationChannel("scheduledExpensesChannelId", "Scheduled Expenses", NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("A scheduled expense is due. Choose to pay or cancel.");
        manager.createNotificationChannel(channel);

        for (ScheduledNotification notification : scheduledExpenses) {
            if (notification.getEnabled()) {
                Intent myIntent = new Intent(context, ScheduledNotificationReceiver.class);
                myIntent.putExtra(ScheduledNotificationReceiver.SCHEDULED_NOTIFICATION_EXTRA, notification);
                scheduleNotification(notification, context, myIntent, alarmManager);
            }
        }
    }

    private void scheduleNotification(ScheduledNotification scheduledNotification, Context context, Intent myIntent, AlarmManager alarmManager) {
        int requestId = Math.toIntExact(scheduledNotification.getId());
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, requestId, myIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
        }

        Log.i(TAG, "scheduleNotification: " + scheduledNotification.getNote());
        if (scheduledNotification.isRepeating())
            alarmManager.setInexactRepeating(AlarmManager.RTC, scheduledNotification.getTimestamp(), scheduledNotification.getPeriod(), pendingIntent);
        else alarmManager.set(AlarmManager.RTC, scheduledNotification.getTimestamp(), pendingIntent);
    }

    private void cancelAllAlarms(AlarmManager alarmManager, Context context) {
        Intent myIntent = new Intent(context, ScheduledNotificationReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 1, myIntent, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE);
        if (pendingIntent != null)
            alarmManager.cancel(pendingIntent);
    }
}
