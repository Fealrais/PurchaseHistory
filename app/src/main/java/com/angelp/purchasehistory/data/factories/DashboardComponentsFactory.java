package com.angelp.purchasehistory.data.factories;

import com.angelp.purchasehistory.data.interfaces.RefreshablePurchaseFragment;
import com.angelp.purchasehistory.ui.home.dashboard.graph.AccumulativeChartFragment;
import com.angelp.purchasehistory.ui.home.dashboard.graph.BarChartFragment;
import com.angelp.purchasehistory.ui.home.dashboard.graph.LineChartFragment;
import com.angelp.purchasehistory.ui.home.dashboard.list.PurchaseListDashboardFragment;
import com.angelp.purchasehistory.ui.home.dashboard.pie.PieChartFragment;

public final class DashboardComponentsFactory {
    public static RefreshablePurchaseFragment createFragment(String type) {
        return switch (type) {
            case "PieChartFragment" -> new PieChartFragment();
            case "PurchaseListPurchaseFragment" -> new PurchaseListDashboardFragment();
            case "LineChartFragment" -> new LineChartFragment();
            case "AccumulativeChartFragment" -> new AccumulativeChartFragment();
            case "BarChartFragment" -> new BarChartFragment();
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }
}
