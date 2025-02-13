package com.angelp.purchasehistory.ui.home.dashboard;

import androidx.fragment.app.Fragment;
import com.angelp.purchasehistory.data.filters.PurchaseFilter;
import com.angelp.purchasehistory.data.interfaces.RefreshablePurchaseFragment;

import java.util.function.Consumer;

public abstract class RefreshableFragment extends Fragment implements RefreshablePurchaseFragment {
    protected PurchaseFilter filter;
    protected final Consumer<PurchaseFilter> setFilter;

    public RefreshableFragment(PurchaseFilter filter, Consumer<PurchaseFilter> setFilter) {
        this.filter = filter;
        this.setFilter = setFilter;
    }
}
