package com.angelp.purchasehistory.receivers.scheduled;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.angelp.purchasehistory.data.model.ScheduledNotification;
import com.angelp.purchasehistory.web.clients.ScheduledExpenseClient;
import dagger.hilt.android.AndroidEntryPoint;

import javax.inject.Inject;

@AndroidEntryPoint
public class TriggerScheduledExpenseReceiver extends BroadcastReceiver {
    public static final String NOTIFICATION_EXTRA_ARG = "clickedNotificationTag";
    @Inject
    ScheduledExpenseClient scheduledExpenseClient;
    /**
     * @param context The Context in which the receiver is running.
     * @param intent  The Intent being received.
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("TriggerScheduledExpenseReceiver", "Received intent");
        ScheduledNotification notification = intent.getParcelableExtra(NOTIFICATION_EXTRA_ARG);
        if (notification == null || intent.getAction() == null) {
            Log.i("TriggerScheduledExpenseReceiver", "Invalid notification or action");
            return;
        }
        if (intent.getAction().equals("trigger")) {
            new Thread(()->{
                scheduledExpenseClient.triggerScheduledPurchase(notification.getId());
            }).start();
        } else {
            Log.i("TriggerScheduledExpenseReceiver", "Dismissed for ID: " + notification.getId());
        }
    }
}
