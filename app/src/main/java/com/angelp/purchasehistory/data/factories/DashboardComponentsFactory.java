package com.angelp.purchasehistory.data.factories;

import com.angelp.purchasehistory.data.interfaces.RefreshablePurchaseFragment;
import com.angelp.purchasehistory.ui.home.dashboard.graph.BarChartFragment;
import com.angelp.purchasehistory.ui.home.dashboard.graph.LineChartFragment;
import com.angelp.purchasehistory.ui.home.dashboard.list.PurchaseListPurchaseFragment;
import com.angelp.purchasehistory.ui.home.dashboard.pie.PieChartFragment;

public final class DashboardComponentsFactory {
    public static RefreshablePurchaseFragment createFragment(String type) {
        return switch (type) {
            case "PieChartFragment" -> new PieChartFragment();
            case "PurchaseListPurchaseFragment" -> new PurchaseListPurchaseFragment();
            case "LineChartFragment" -> new LineChartFragment();
            case "BarChartFragment" -> new BarChartFragment();
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }
}
