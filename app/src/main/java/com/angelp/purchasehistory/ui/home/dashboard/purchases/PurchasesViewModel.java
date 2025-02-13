package com.angelp.purchasehistory.ui.home.dashboard.purchases;

import androidx.lifecycle.ViewModel;
import com.angelp.purchasehistorybackend.models.views.outgoing.PurchaseView;
import com.angelp.purchasehistory.web.clients.PurchaseClient;
import dagger.hilt.android.lifecycle.HiltViewModel;
import lombok.Getter;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@Getter
@HiltViewModel
public class PurchasesViewModel extends ViewModel {
    PurchaseClient purchaseClient;
    private List<PurchaseView> allPurchases = new ArrayList<>();


    @Inject
    public PurchasesViewModel(PurchaseClient purchaseClient) {
        this.purchaseClient = purchaseClient;
        new Thread(()-> this.allPurchases = purchaseClient.getAllPurchases());

    }

}
