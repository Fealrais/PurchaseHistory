package com.example.purchasehistory.ui.home.qr;

import androidx.lifecycle.MutableLiveData;
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
    private final MutableLiveData<PurchaseDTO> purchaseDTO = new MutableLiveData<>(new PurchaseDTO());

    PurchaseClient purchaseClient;

    public PurchaseDTO getCurrentPurchaseDTO() {
        if(purchaseDTO.getValue() == null) return new PurchaseDTO();
        return purchaseDTO.getValue();
    }

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
        PurchaseDTO prev = getCurrentPurchaseDTO();
        if(purchaseDTO.getQrContent()!= null) prev.setQrContent(purchaseDTO.getQrContent());
        if(purchaseDTO.getPrice()!= null) prev.setPrice(purchaseDTO.getPrice());
        if(purchaseDTO.getTimestamp()!= null) prev.setTimestamp(purchaseDTO.getTimestamp());
        if(purchaseDTO.getCategoryId()!= null) prev.setCategoryId(purchaseDTO.getCategoryId());
        if(purchaseDTO.getBillId()!= null) prev.setBillId(purchaseDTO.getBillId());
        if(purchaseDTO.getStoreId()!= null) prev.setStoreId(purchaseDTO.getStoreId());
        this.purchaseDTO.postValue(prev);
    }
}