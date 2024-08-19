package com.example.purchasehistory.ui.home.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.purchasehistory.data.filters.PurchaseFilter;
import com.example.purchasehistory.data.interfaces.RefreshablePurchaseFragment;
import com.example.purchasehistory.databinding.FragmentDashboardBinding;
import com.example.purchasehistory.ui.home.dashboard.list.PurchaseListPurchaseFragment;
import com.example.purchasehistory.ui.home.dashboard.pie.PieChartFragment;
import com.example.purchasehistory.ui.home.purchases.PurchaseFilterDialog;
import dagger.hilt.android.AndroidEntryPoint;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import static com.example.purchasehistory.data.Constants.getDefaultFilter;

@NoArgsConstructor
@AndroidEntryPoint
public class DashboardFragment extends Fragment {
    private final String DASHBOARD_FILTER = "dashboard_filter";
    private final String TAG = this.getClass().getSimpleName();
    private FragmentDashboardBinding binding;
    private PurchaseFilterDialog filterDialog;
    private PurchaseFilter filter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        filterDialog = new PurchaseFilterDialog();
        this.applyFilter(filter);
        getParentFragmentManager()
                .beginTransaction()
                .replace(binding.pieChartFragmentContainer.getId(), new PieChartFragment(filter,
                        (newFilter) -> {
                            applyFilter(newFilter);
                            refreshFragment(newFilter, binding.listedPurchasesFragmentContainer.getId());
                        }))
                .replace(binding.listedPurchasesFragmentContainer.getId(), new PurchaseListPurchaseFragment(filter))
                .commit();
        binding.dashboardFilterButton.setOnClickListener(v -> openFilter(this::updateFilter));
        return binding.getRoot();
    }

    private PurchaseFilter loadFilterArg(Bundle bundle) {
        if (bundle == null || bundle.getParcelable(DASHBOARD_FILTER) == null)
            return getDefaultFilter();
        return bundle.getParcelable(DASHBOARD_FILTER);
    }

    private void applyFilter(PurchaseFilter newFilter) {
        binding.dashboardFilterButton.setText(newFilter.isEmpty() ? "Filter" : "Filtered");
        binding.dashboardFilterDateText.setText(filter.getReadableString());
    }

    private void openFilter(Consumer<PurchaseFilter> setFilter) {
        if (filterDialog.getFilter() == null)
            filterDialog.setFilter(getDefaultFilter());
        filterDialog.show(getParentFragmentManager(), "purchasesFilterDialog");
        filterDialog.setOnSuccess(setFilter);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.filter = loadFilterArg(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(@NonNull @NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(DASHBOARD_FILTER, filter);
    }

    private void refresh(PurchaseFilter filter) {
        refreshFragment(filter, binding.pieChartFragmentContainer.getId());
        refreshFragment(filter, binding.listedPurchasesFragmentContainer.getId());
    }

    private void refreshFragment(PurchaseFilter filter, int id) {
        RefreshablePurchaseFragment purchaseListFragment = (RefreshablePurchaseFragment) getParentFragmentManager().findFragmentById(id);
        if (purchaseListFragment != null) purchaseListFragment.refresh(filter);
    }


    private void updateFilter(PurchaseFilter newFilter) {
        this.filter = newFilter;
        this.applyFilter(newFilter);
        if (filterDialog.isAdded())
            filterDialog.dismiss();
        refresh(newFilter);
    }
}