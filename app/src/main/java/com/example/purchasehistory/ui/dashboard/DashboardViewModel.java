package com.example.purchasehistory.ui.dashboard;

import androidx.lifecycle.ViewModel;
import com.angelp.purchasehistorybackend.models.views.outgoing.PurchaseView;
import com.example.purchasehistory.PurchaseHistoryApplication;
import com.example.purchasehistory.web.clients.PurchaseClient;
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
            PurchaseHistoryApplication.getInstance().alert(e.getMessage());
            return new ArrayList<>();
        }
    }

}