package com.example.purchasehistory.ui.qr;

import androidx.lifecycle.ViewModel;
import com.example.purchasehistory.web.clients.PurchaseClient;
import dagger.hilt.android.lifecycle.HiltViewModel;

import javax.inject.Inject;

@HiltViewModel
public class QrScannerViewModel extends ViewModel {

    PurchaseClient purchaseClient;

    @Inject
    public QrScannerViewModel(PurchaseClient purchaseClient) {
        this.purchaseClient = purchaseClient;
    }

    public Runnable createPurchaseView(String qrContent) {
        return () -> purchaseClient.createPurchase(qrContent);
    }

}