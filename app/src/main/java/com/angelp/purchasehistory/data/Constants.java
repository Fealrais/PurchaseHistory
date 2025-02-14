package com.angelp.purchasehistory.data;

import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.data.filters.PurchaseFilter;
import com.angelp.purchasehistory.data.model.DashboardComponent;
import com.angelp.purchasehistory.data.tour.TourStep;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class Constants {
    public static final String PURCHASE_EDIT_DIALOG_ID_KEY = "purchaseId";
    public static final String ARG_SHOW_FILTER = "show_filter";
    public static final String APP_BOOT_RECEIVER = "APP_BOOT_RECEIVER";
    public static final String NOTIFICATION_EXTRA_ARG = "scheduledNotifications";
    public static final String ARG_MAX_SIZE = "max_size";
    public static final int GRAPH_MIN_HEIGHT = 1000;
    public static final String ARG_COMPONENT = "component";
    public final static List<DashboardComponent> DEFAULT_COMPONENTS = new ArrayList<>();
    public static final String DASHBOARD_FRAGMENT = "dashboardFragment";
    public static final String DASHBOARD_PREFS = "dashboard_prefs";
    public static final String IS_FIRST_TIME_OPEN = "isFirstTimeOpen_HomeActivity";
    public static final String APP_PREFERENCES = "app_preferences";
    public static final List<TourStep> tourSteps = new ArrayList<>();
    public static final String DASHBOARD_FILTER = "dashboard_filter";

    static {
        DEFAULT_COMPONENTS.add(new DashboardComponent("PieChartFragment"));
        DEFAULT_COMPONENTS.add(new DashboardComponent("LineChartFragment"));
        DEFAULT_COMPONENTS.add(new DashboardComponent("BarChartFragment"));
        DEFAULT_COMPONENTS.add(new DashboardComponent("PurchaseListPurchaseFragment"));
    }

    static {
        tourSteps.add(new TourStep(R.id.navigation_dashboard, R.string.tour_navigation_dashboard, R.string.tour_navigation_dashboard_secondary));
        tourSteps.add(new TourStep(R.id.dashboard_filterButton, R.string.tour_filter_button, R.string.tour_filter_button_secondary));
        tourSteps.add(new TourStep(R.id.navigation_qrscanner, R.string.tour_navigation_qrscanner, R.string.tour_navigation_qrscanner_secondary));
        tourSteps.add(new TourStep(R.id.navigation_scheduled_expenses, R.string.tour_navigation_scheduled_expenses, R.string.tour_navigation_scheduled_expenses_secondary));
        tourSteps.add(new TourStep(R.id.navigation_profile, R.string.tour_navigation_profile, R.string.tour_navigation_profile_secondary));
    }

    public static PurchaseFilter getDefaultFilter() {
        return new PurchaseFilter(LocalDate.now().withDayOfMonth(1), LocalDate.now(), null, null);
    }
}
