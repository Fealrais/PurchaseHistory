package com.angelp.purchasehistory.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.angelp.purchasehistory.data.Constants;
import com.angelp.purchasehistory.data.model.ScheduledNotification;
import com.angelp.purchasehistory.receivers.scheduled.InitiateNotificationReceiver;
import com.angelp.purchasehistory.web.clients.ScheduledExpenseClient;
import com.angelp.purchasehistorybackend.models.views.outgoing.ScheduledExpenseView;
import dagger.hilt.android.AndroidEntryPoint;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@AndroidEntryPoint
public class AppBootReceiver extends BroadcastReceiver {
    @Inject
    ScheduledExpenseClient scheduledExpenseClient;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) || Constants.APP_BOOT_RECEIVER.equals(intent.getAction())) {
            new Thread(() -> {
                List<ScheduledExpenseView> allForUser = scheduledExpenseClient.findAllForUser();
                Intent notificationIntent = new Intent(context, InitiateNotificationReceiver.class);
                notificationIntent.setAction(Constants.APP_BOOT_RECEIVER);
                ArrayList<ScheduledNotification> list = new ArrayList<>();
                for (ScheduledExpenseView scheduledExpenseView : allForUser) {
                    ScheduledNotification scheduledNotification = new ScheduledNotification(scheduledExpenseView);
                    list.add(scheduledNotification);
                }
                notificationIntent.putParcelableArrayListExtra(Constants.Arguments.NOTIFICATION_EXTRA_ARG,list);
                context.sendBroadcast(notificationIntent);
            }).start();
        }
    }
}
