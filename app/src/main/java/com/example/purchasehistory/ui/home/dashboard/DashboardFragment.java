package com.example.purchasehistory.ui.home.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.purchasehistory.data.filters.PurchaseFilter;
import com.example.purchasehistory.databinding.FragmentDashboardBinding;
import com.example.purchasehistory.ui.home.dashboard.pie.PieChartFragment;
import com.example.purchasehistory.ui.home.purchases.PurchaseFilterDialog;
import dagger.hilt.android.AndroidEntryPoint;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.function.Consumer;

@AndroidEntryPoint
public class DashboardFragment extends Fragment {
    private final String DASHBOARD_FILTER = "dashboard_filter";
    private final String TAG = this.getClass().getSimpleName();
    private final DateTimeFormatter dtf = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);
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
                .replace(binding.fragmentContainerView.getId(), PieChartFragment.newInstance(filter))
                .commit();
        binding.dashboardFilterButton.setOnClickListener(v -> openFilter((newFilter) -> {
            this.filter = newFilter;
            this.applyFilter(newFilter);
            filterDialog.dismiss();
            onSwipeRefresh(newFilter);
        }));
        return binding.getRoot();
    }

    private PurchaseFilter loadFilterArg(Bundle bundle) {
        if (bundle == null || bundle.getParcelable(DASHBOARD_FILTER) == null)
            return new PurchaseFilter();
        return bundle.getParcelable(DASHBOARD_FILTER);
    }

    private void applyFilter(PurchaseFilter newFilter) {
        binding.dashboardFilterButton.setText(newFilter.isEmpty() ? "Filter" : "Filtered");
        LocalDate from = filter.getFrom() != null ? filter.getFrom() : LocalDate.now().withDayOfMonth(1);
        LocalDate filterTo = filter.getTo() != null ? filter.getTo() : LocalDate.now();
        binding.dashboardFilterDateText.setText(String.format("Showing period of %s - %s", from.format(dtf), filterTo.format(dtf)));
    }

    private void openFilter(Consumer<PurchaseFilter> setFilter) {
        if (filterDialog.getFilter() == null)
            filterDialog.setFilter(new PurchaseFilter());
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

    private void onSwipeRefresh(PurchaseFilter filter) {
        PieChartFragment fragment = binding.fragmentContainerView.getFragment();
        fragment.refresh(filter);
    }


}