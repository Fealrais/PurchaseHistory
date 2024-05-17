package com.example.purchasehistory.data.model;

import com.angelp.purchasehistorybackend.models.views.outgoing.CategoryView;
import com.angelp.purchasehistorybackend.models.views.outgoing.PurchaseView;
import com.angelp.purchasehistorybackend.models.views.outgoing.UserView;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class PurchaseResponse {
    private String qrContent;
    private BigDecimal price;
    private String timestamp;
    private String billId;
    private String storeId;

    private UserView createdBy;
    private String createdDate;

    private UserView lastModifiedBy;
    private String lastModifiedDate;

    private CategoryView category;

    public PurchaseView toPurchaseView() {
        return new PurchaseView(
                qrContent,
                price,
                timestamp == null ? null : LocalDateTime.parse(timestamp),
                billId,
                storeId,
                category,
                createdBy,
                createdDate == null ? null : LocalDateTime.parse(createdDate),
                lastModifiedBy,
                lastModifiedDate == null ? null : LocalDateTime.parse(lastModifiedDate));
    }
}
