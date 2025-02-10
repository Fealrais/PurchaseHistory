package com.angelp.purchasehistory.receivers.scheduled;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import androidx.core.app.NotificationCompat;
import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.data.model.ScheduledNotification;

public class ScheduledNotificationReceiver extends BroadcastReceiver {
    public static final String SCHEDULED_NOTIFICATION_EXTRA = "SCHEDULED_NOTIFICATION_EXTRA";
    private static final String CHANNEL_ID = "scheduledExpensesChannelId";
    private static final String ACTION_TRIGGER = "trigger";
    private static final String TAG = "ScheduledNotificationReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Received intent");

        ScheduledNotification scheduledNotification = intent.getParcelableExtra(SCHEDULED_NOTIFICATION_EXTRA);
        if(scheduledNotification == null ) {
            Log.e(TAG, "Invalid list");
            return;
        }
        Long id = scheduledNotification.getId();
        Log.d(TAG, "Scheduling notification: " + scheduledNotification);

        Intent triggerIntent = new Intent(context, TriggerScheduledExpenseReceiver.class);
        triggerIntent.setAction(ACTION_TRIGGER);
        triggerIntent.putExtra(TriggerScheduledExpenseReceiver.NOTIFICATION_EXTRA_ARG, scheduledNotification);
        PendingIntent triggerPendingIntent =
                PendingIntent.getBroadcast(context, 0, triggerIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_scheduled_payment)
                .setContentText(context.getString(R.string.scheduled_notification_content, scheduledNotification.getPrice()))
                .addAction(R.drawable.baseline_attach_money_24, context.getString(R.string.add_purchase_action), triggerPendingIntent)
                .setContentTitle(context.getString(R.string.scheduled_notification_title, scheduledNotification.getNote()))
                .build();

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Log.d(TAG, "Sending notification with ID: " + id);
        manager.notify(id.intValue(), notification);
    }
}
