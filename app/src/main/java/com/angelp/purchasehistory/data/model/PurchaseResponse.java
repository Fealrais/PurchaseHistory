package com.angelp.purchasehistory.data.model;

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
    private Long id;
    private String qrContent;
    private BigDecimal price;
    private String timestamp;
    private String billId;
    private String storeId;
    private String note;

    private UserView createdBy;
    private String createdDate;

    private UserView lastModifiedBy;
    private String lastModifiedDate;

    private CategoryView category;

    public PurchaseView toPurchaseView() {
        return new PurchaseView(
                id,
                qrContent,
                price,
                timestamp == null ? null : LocalDateTime.parse(timestamp),
                billId,
                storeId,
                note,
                category,
                createdBy,
                createdDate == null ? null : LocalDateTime.parse(createdDate),
                lastModifiedBy,
                lastModifiedDate == null ? null : LocalDateTime.parse(lastModifiedDate));
    }
}
