package com.angelp.purchasehistory.receivers.scheduled;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import com.angelp.purchasehistory.data.Constants;
import com.angelp.purchasehistory.data.model.ScheduledNotification;
import com.angelp.purchasehistorybackend.models.views.outgoing.ScheduledExpenseView;

import java.time.Instant;
import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class NotificationHelper {
    private static final String TAG = "SchaeduledExpenseNotification";

    public static void cancelAllScheduledNotifications(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ScheduledNotificationReceiver.class);
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.Preferences.SILENCED_NOTIFICATIONS, MODE_PRIVATE);
        for (String id : sharedPreferences.getAll().keySet()) {
            intent.setIdentifier("com.angelp.purchasehistory.SCHEDULED_NOTIFY_" + id);
            try {
                PendingIntent pi = PendingIntent.getBroadcast(
                        context,
                        Integer.parseInt(id),
                        intent,
                        PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_IMMUTABLE
                );
                if (pi != null) {
                    Log.i("ScheduledExpense", "cancelAllScheduledNotifications: Cancelling notification with id"+id );

                    am.cancel(pi);
                    pi.cancel();
                }
            } catch (NumberFormatException e) {
                Log.e("ScheduledExpense", "cancelAllScheduledNotifications: Invalid number format in id!" );
            }
        }
        sharedPreferences.edit().clear().apply();
    }

    public static void reschedule(ScheduledNotification notification, Context context) {
        Log.i(TAG, "Notification is repeating.");
        long period = notification.getPeriod();
        long now = System.currentTimeMillis();
        long nextTrigger = notification.getTimestamp();
        while (nextTrigger <= now) {
            nextTrigger += period;
        }
        notification.setTimestamp(nextTrigger);
        Intent reschedule = new Intent(context, InitiateNotificationReceiver.class);
        ArrayList<ScheduledNotification> list = new ArrayList<>();
        list.add(notification);
        reschedule.putParcelableArrayListExtra(Constants.Arguments.NOTIFICATION_EXTRA_ARG, list);
        context.sendBroadcast(reschedule);
        Log.i(TAG, "reschedule: Rescheduled notification" + notification.getId() + " to " + Instant.ofEpochMilli(nextTrigger));
    }

    public static void deleteAlarm(Context context, ScheduledExpenseView newExpense) {
        if (context == null) return;
        ScheduledNotification n = new ScheduledNotification(newExpense);
        Intent intent = new Intent(context, ScheduledNotificationReceiver.class);
        int requestCode = Math.toIntExact(n.getId());

        intent.putExtra(ScheduledNotificationReceiver.SCHEDULED_NOTIFICATION_EXTRA, n);
        intent.setAction("com.angelp.purchasehistory.SCHEDULED_NOTIFY_" + n.getId());

        PendingIntent pi = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );
        pi.cancel();
        Log.i("ScheduledExpense", "deleteAlarm: Deleted scheduled notification for item" + n.getId());
    }

    public static void addNotificationAlarm(Context context, ScheduledExpenseView newExpense) {
        if (context == null) return;
        Log.i("ScheduledExpense", "createAlarm: Deleted scheduled notification for item" + newExpense.getId());
        ScheduledNotification scheduledNotification = new ScheduledNotification(newExpense);
        Intent intent = new Intent(context, InitiateNotificationReceiver.class);
        ArrayList<ScheduledNotification> list = new ArrayList<>();
        list.add(scheduledNotification);
        intent.putParcelableArrayListExtra(Constants.Arguments.NOTIFICATION_EXTRA_ARG, list);
        context.sendBroadcast(intent);

    }
}
