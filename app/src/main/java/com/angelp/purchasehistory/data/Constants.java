package com.angelp.purchasehistory.data;

import com.angelp.purchasehistory.data.filters.PurchaseFilter;

import java.time.LocalDate;

public final class Constants {
    public static final String PURCHASE_EDIT_DIALOG_ID_KEY = "purchaseId";
    public static final String ARG_SHOW_FILTER = "show_filter";
    public static final String APP_BOOT_RECEIVER = "APP_BOOT_RECEIVER";
    public static final String NOTIFICATION_EXTRA_ARG = "scheduledNotifications";
    public static final String ARG_FILTER = "purchase_filter";
    public static final String ARG_MAX_SIZE = "max_size";
    public static final int GRAPH_MIN_HEIGHT = 1000;
    public static final String ARG_COMPONENT = "component";

    public static PurchaseFilter getDefaultFilter() {
        return new PurchaseFilter(LocalDate.now().withDayOfMonth(1), LocalDate.now(), null, null);
    }
}
