package com.angelp.purchasehistory.ui.home.dashboard;

import androidx.lifecycle.ViewModel;
import com.angelp.purchasehistory.PurchaseHistoryApplication;
import com.angelp.purchasehistory.data.filters.PurchaseFilter;
import com.angelp.purchasehistory.web.clients.PurchaseClient;
import com.angelp.purchasehistorybackend.models.views.outgoing.PurchaseView;
import com.angelp.purchasehistorybackend.models.views.outgoing.analytics.CategoryAnalyticsReport;
import dagger.hilt.android.lifecycle.HiltViewModel;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@HiltViewModel
public class DashboardViewModel extends ViewModel {

    PurchaseClient purchaseClient;

    @Inject
    public DashboardViewModel(PurchaseClient purchaseClient) {
        this.purchaseClient = purchaseClient;
    }


    public List<PurchaseView> getAllPurchases() {
        try {
            return purchaseClient.getAllPurchases();
        } catch (RuntimeException e) {
            PurchaseHistoryApplication.getInstance().alert("Error " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public CategoryAnalyticsReport getCategoryAnalyticsReport(PurchaseFilter filter) {
        return purchaseClient.getCategoryAnalyticsReport(filter);
    }
}