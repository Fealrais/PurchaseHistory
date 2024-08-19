package com.example.purchasehistory.data;

import com.example.purchasehistory.data.filters.PurchaseFilter;

import java.time.LocalDate;

public final class Constants {
    public static final String PURCHASE_EDIT_DIALOG_ID_KEY = "purchaseId";

    public static PurchaseFilter getDefaultFilter() {
        return new PurchaseFilter(LocalDate.now().withDayOfMonth(1), LocalDate.now(), null, null);
    }
}
