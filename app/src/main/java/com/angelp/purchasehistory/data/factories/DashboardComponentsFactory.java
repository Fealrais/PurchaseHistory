package com.angelp.purchasehistory.data.factories;

import com.angelp.purchasehistory.data.filters.PurchaseFilter;
import com.angelp.purchasehistory.ui.home.dashboard.RefreshableFragment;
import com.angelp.purchasehistory.ui.home.dashboard.list.PurchaseListPurchaseFragment;
import com.angelp.purchasehistory.ui.home.dashboard.pie.PieChartFragment;
import com.angelp.purchasehistory.ui.home.dashboard.graph.BarChartFragment;
import com.angelp.purchasehistory.ui.home.dashboard.graph.LineChartFragment;

import java.util.function.Consumer;

public final class DashboardComponentsFactory {
    public static RefreshableFragment createFragment(String type, PurchaseFilter filter, Consumer<PurchaseFilter> setFilter) {
        return switch (type) {
            case "PieChartFragment" -> new PieChartFragment(filter, setFilter);
            case "PurchaseListPurchaseFragment" -> new PurchaseListPurchaseFragment(filter, setFilter);
            case "LineChartFragment" -> new LineChartFragment(filter, setFilter);
            case "BarChartFragment" -> new BarChartFragment(filter, setFilter);
            default -> throw new IllegalStateException("Unexpected value: " + type);
        };
    }
}
