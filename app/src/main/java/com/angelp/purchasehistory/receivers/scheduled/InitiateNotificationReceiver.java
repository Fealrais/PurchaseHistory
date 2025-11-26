package com.angelp.purchasehistory.receivers.scheduled;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;
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

        SharedPreferences.Editor editor = preferences.edit();
        for (ScheduledNotification notification : scheduledExpenses) {
            boolean isSilenced = preferences.getBoolean(notification.getId().toString(), false);
            editor.putBoolean(notification.getId().toString(), isSilenced);
            Intent myIntent = new Intent(context, ScheduledNotificationReceiver.class);
            scheduleNotification(notification, context, myIntent, alarmManager, notification.getEnabled() && !isSilenced);
        }
        editor.apply();
    }

    private void scheduleNotification(ScheduledNotification n, Context context, Intent intent, AlarmManager am, boolean active) {
        int requestCode = Math.toIntExact(n.getId());


        intent.putExtra(ScheduledNotificationReceiver.SCHEDULED_NOTIFICATION_EXTRA, n);
        intent.setIdentifier("com.angelp.purchasehistory.SCHEDULED_NOTIFY_" + n.getId());

        PendingIntent pi = PendingIntent.getBroadcast(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        if (active) {
            long triggerAt = n.getTimestamp();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                // Required on Android 12+ if you want exact alarms
                if (!am.canScheduleExactAlarms()) {
                    // Ask user to grant it in settings – otherwise fall back
                    Intent i = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(i);
                }
            }
            am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pi);
            Log.i(TAG, "scheduleNotification:  Scheduled notification " + pi);

        } else {
            Log.i(TAG, "scheduleNotification: Cancelling notification " + pi);
            am.cancel(pi);
            pi.cancel();
        }
    }
}
