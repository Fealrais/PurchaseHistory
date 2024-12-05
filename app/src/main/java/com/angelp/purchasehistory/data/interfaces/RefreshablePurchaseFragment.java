package com.angelp.purchasehistory.data.interfaces;

import com.angelp.purchasehistory.data.filters.PurchaseFilter;

public interface RefreshablePurchaseFragment {
    void refresh(PurchaseFilter filter);
}
