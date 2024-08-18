package com.example.purchasehistory.ui.home.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.purchasehistory.data.filters.PurchaseFilter;
import com.example.purchasehistory.databinding.FragmentDashboardBinding;
import com.example.purchasehistory.ui.home.dashboard.pie.PieChartFragment;
import com.example.purchasehistory.ui.home.purchases.PurchaseFilterDialog;
import dagger.hilt.android.AndroidEntryPoint;
import lombok.Setter;

import java.util.function.Consumer;

@AndroidEntryPoint
public class DashboardFragment extends Fragment {
    private final String TAG = this.getClass().getSimpleName();
    private FragmentDashboardBinding binding;
    private PurchaseFilterDialog filterDialog;
    @Setter
    private PurchaseFilter filter = new PurchaseFilter();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        filterDialog = new PurchaseFilterDialog();

        getParentFragmentManager()
                .beginTransaction()
                .replace(binding.fragmentContainerView.getId(), PieChartFragment.newInstance(filter))
                .commit();
        binding.dashboardFilterButton.setOnClickListener(v -> openFilter((newFilter) -> {
            setFilter(newFilter);
            binding.dashboardFilterButton.setText(newFilter.isEmpty() ? "Filter" : "Filtered");
            filterDialog.dismiss();
            onSwipeRefresh(filter);
        }));
        return binding.getRoot();
    }

    private void openFilter(Consumer<PurchaseFilter> setFilter) {
        if (filterDialog.getFilter() == null)
            filterDialog.setFilter(new PurchaseFilter());
        filterDialog.show(getParentFragmentManager(), "purchasesFilterDialog");
        filterDialog.setOnSuccess(setFilter);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void onSwipeRefresh(PurchaseFilter filter) {
        PieChartFragment fragment = binding.fragmentContainerView.getFragment();
        fragment.refresh(filter);
    }


}