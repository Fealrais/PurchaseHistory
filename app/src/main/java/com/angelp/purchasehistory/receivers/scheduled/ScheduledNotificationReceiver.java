package com.angelp.purchasehistory.receivers.scheduled;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.data.model.ScheduledNotification;

import static android.content.Context.NOTIFICATION_SERVICE;

public class ScheduledNotificationReceiver extends BroadcastReceiver {
    public static final String SCHEDULED_NOTIFICATION_EXTRA = "SCHEDULED_NOTIFICATION_EXTRA";
    public static final String CHANNEL_ID = "scheduledExpensesChannelId";
    private static final String ACTION_TRIGGER = "trigger";
    private static final String TAG = "ScheduledNotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Received intent");

        ScheduledNotification scheduledNotification = intent.getParcelableExtra(SCHEDULED_NOTIFICATION_EXTRA);
        if (scheduledNotification == null) {
            Log.e(TAG, "Invalid list");
            return;
        }
        Long id = scheduledNotification.getId();
        Log.d(TAG, "Scheduling notification: " + scheduledNotification);

        Intent triggerIntent = new Intent(context, TriggerScheduledExpenseReceiver.class);
        triggerIntent.setAction(ACTION_TRIGGER);
        triggerIntent.putExtra(TriggerScheduledExpenseReceiver.NOTIFICATION_EXTRA_ARG, scheduledNotification);
        PendingIntent triggerPendingIntent =
                PendingIntent.getBroadcast(context, 0, triggerIntent, PendingIntent.FLAG_IMMUTABLE);
        NotificationManager manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        NotificationChannel channel = getOrCreateChannel(manager);

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_scheduled_payment)
                .setContentText(context.getString(R.string.scheduled_notification_content, scheduledNotification.getPrice()))
                .addAction(R.drawable.baseline_attach_money_24, context.getString(R.string.add_purchase_action), triggerPendingIntent)
//                .addAction(R.drawable.baseline_money_off_24, context.getString(R.string.cancel), null)
                .setContentTitle(context.getString(R.string.scheduled_notification_title, scheduledNotification.getNote()))
                .setColor(scheduledNotification.getColor())
                .build();
        Log.d(TAG, "Sending notification with ID: " + id);
        manager.notify(id.intValue(), notification);
        if (scheduledNotification.isRepeating()) {
            NotificationHelper.reschedule(scheduledNotification, context);
        }
    }

    private NotificationChannel getOrCreateChannel(NotificationManager manager) {
        NotificationChannel existing = manager.getNotificationChannel(CHANNEL_ID);
        if (existing != null) return existing;
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Scheduled Expenses", NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("Scheduled expenses");
        manager.createNotificationChannel(channel);
        return channel;
    }
}
