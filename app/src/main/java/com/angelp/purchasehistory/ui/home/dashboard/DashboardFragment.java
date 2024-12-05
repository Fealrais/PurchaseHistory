package com.angelp.purchasehistory.ui.home.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.angelp.purchasehistory.data.filters.PurchaseFilter;
import com.angelp.purchasehistory.data.interfaces.RefreshablePurchaseFragment;
import com.angelp.purchasehistory.databinding.FragmentDashboardBinding;
import com.angelp.purchasehistory.ui.home.dashboard.list.PurchaseListPurchaseFragment;
import com.angelp.purchasehistory.ui.home.dashboard.pie.PieChartFragment;
import com.angelp.purchasehistory.ui.home.purchases.PurchaseFilterDialog;
import dagger.hilt.android.AndroidEntryPoint;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

import static com.angelp.purchasehistory.data.Constants.getDefaultFilter;

@NoArgsConstructor
@AndroidEntryPoint
public class DashboardFragment extends Fragment implements RefreshablePurchaseFragment{
    private final String DASHBOARD_FILTER = "dashboard_filter";
    private final String TAG = this.getClass().getSimpleName();
    private FragmentDashboardBinding binding;
    private PurchaseFilterDialog filterDialog;
    @Setter
    private PurchaseFilter filter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        filterDialog = new PurchaseFilterDialog(false);
        this.applyFilter(filter);
        getParentFragmentManager()
                .beginTransaction()
                .replace(binding.pieChartFragmentContainer.getId(), new PieChartFragment(filter,
                        (newFilter) -> {
                            applyFilter(newFilter);
                            refreshFragment(newFilter, binding.listedPurchasesFragmentContainer.getId());
                        }))
                .replace(binding.listedPurchasesFragmentContainer.getId(), new PurchaseListPurchaseFragment(filter, ()->refresh(filter)))
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

    public void refresh(PurchaseFilter filter) {
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