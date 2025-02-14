package com.angelp.purchasehistory.data.interfaces;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.angelp.purchasehistory.data.filters.PurchaseFilter;
import com.angelp.purchasehistory.data.filters.PurchaseFilterSingleton;
import dagger.hilt.android.AndroidEntryPoint;

import javax.inject.Inject;

@AndroidEntryPoint
public abstract class RefreshablePurchaseFragment extends Fragment {
    @Inject
    protected PurchaseFilterSingleton filterViewModel;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        filterViewModel.getFilter().observe(getViewLifecycleOwner(), this::refresh);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        filterViewModel.getFilter().removeObserver(this::refresh);
    }

    public abstract void refresh(PurchaseFilter filter);
}
