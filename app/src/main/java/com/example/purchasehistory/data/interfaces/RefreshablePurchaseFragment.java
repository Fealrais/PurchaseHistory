package com.example.purchasehistory.data.interfaces;

import com.example.purchasehistory.data.filters.PurchaseFilter;

public interface RefreshablePurchaseFragment {
    void refresh(PurchaseFilter filter);
}
