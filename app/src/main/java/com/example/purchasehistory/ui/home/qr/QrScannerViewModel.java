package com.example.purchasehistory.ui.home.qr;

import androidx.lifecycle.ViewModel;
import com.angelp.purchasehistorybackend.models.views.incoming.PurchaseDTO;
import com.angelp.purchasehistorybackend.models.views.outgoing.CategoryView;
import com.angelp.purchasehistorybackend.models.views.outgoing.PurchaseView;
import com.example.purchasehistory.PurchaseHistoryApplication;
import com.example.purchasehistory.web.clients.PurchaseClient;
import dagger.hilt.android.lifecycle.HiltViewModel;
import lombok.Getter;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

@HiltViewModel
public class QrScannerViewModel extends ViewModel {
    @Getter
    private final PurchaseDTO purchaseDTO = new PurchaseDTO();

    PurchaseClient purchaseClient;

    @Inject
    public QrScannerViewModel(PurchaseClient purchaseClient) {
        this.purchaseClient = purchaseClient;
    }

    public PurchaseView createPurchaseView(PurchaseDTO data) {
        return purchaseClient.createPurchase(data);
    }

    public List<CategoryView> getAllCategories() {
        try {
            return purchaseClient.getAllCategories();
        } catch (RuntimeException e) {
            PurchaseHistoryApplication.getInstance().alert("Error " + e.getMessage());
            return new ArrayList<>();
        }
    }


    public void updatePurchaseDTO(PurchaseDTO purchaseDTO) {
        if(purchaseDTO.getQrContent()!= null) purchaseDTO.setQrContent(purchaseDTO.getQrContent());
        if(purchaseDTO.getPrice()!= null) purchaseDTO.setPrice(purchaseDTO.getPrice());
        if(purchaseDTO.getTimestamp()!= null) purchaseDTO.setTimestamp(purchaseDTO.getTimestamp());
        if(purchaseDTO.getCategoryId()!= null) purchaseDTO.setCategoryId(purchaseDTO.getCategoryId());
        if(purchaseDTO.getBillId()!= null) purchaseDTO.setBillId(purchaseDTO.getBillId());
        if(purchaseDTO.getStoreId()!= null) purchaseDTO.setStoreId(purchaseDTO.getStoreId());
    }
    public void resetPurchaseDto() {
        purchaseDTO.setQrContent(null);
        purchaseDTO.setPrice(null);
        purchaseDTO.setTimestamp(null);
        purchaseDTO.setCategoryId(null);
        purchaseDTO.setBillId(null);
        purchaseDTO.setStoreId(null);
    }
}