package com.angelp.purchasehistory.ui.home.qr;

import androidx.lifecycle.ViewModel;
import com.angelp.purchasehistory.PurchaseHistoryApplication;
import com.angelp.purchasehistory.web.clients.PurchaseClient;
import com.angelp.purchasehistory.web.clients.WebException;
import com.angelp.purchasehistorybackend.models.views.incoming.PurchaseDTO;
import com.angelp.purchasehistorybackend.models.views.outgoing.CategoryView;
import com.angelp.purchasehistorybackend.models.views.outgoing.PurchaseView;
import dagger.hilt.android.lifecycle.HiltViewModel;
import lombok.Getter;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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

    public void validatePurchaseView(PurchaseDTO data, Consumer<Integer> onFail) {
        new Thread(() -> {
            try {
                purchaseClient.validatePurchase(data);
            } catch (WebException e) {
                onFail.accept(e.getErrorResource());
            }
        }).start();
    }

    public List<CategoryView> getAllCategories() {
        try {
            return purchaseClient.getAllCategories();
        } catch (RuntimeException e) {
            PurchaseHistoryApplication.getInstance().alert("Error " + e.getMessage());
            return new ArrayList<>();
        }
    }


    public void updatePurchaseDTO(PurchaseDTO newData) {
        if (newData.getQrContent() != null) purchaseDTO.setQrContent(newData.getQrContent());
        if (newData.getPrice() != null) purchaseDTO.setPrice(newData.getPrice());
        if (newData.getTimestamp() != null) purchaseDTO.setTimestamp(newData.getTimestamp());
        if (newData.getCategoryId() != null) purchaseDTO.setCategoryId(newData.getCategoryId());
        if (newData.getBillId() != null) purchaseDTO.setBillId(newData.getBillId());
        if (newData.getNote() != null) purchaseDTO.setNote(newData.getNote());
        if (newData.getStoreId() != null) purchaseDTO.setStoreId(newData.getStoreId());
    }

    public void resetPurchaseDto() {
        purchaseDTO.setQrContent(null);
        purchaseDTO.setPrice(BigDecimal.ZERO);
        purchaseDTO.setTimestamp(LocalDateTime.now());
        purchaseDTO.setCategoryId(null);
        purchaseDTO.setBillId(null);
        purchaseDTO.setStoreId(null);
        purchaseDTO.setNote("");
    }
}