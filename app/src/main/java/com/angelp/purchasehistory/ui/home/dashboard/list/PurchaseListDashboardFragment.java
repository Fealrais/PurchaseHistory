package com.angelp.purchasehistory.ui.home.dashboard.list;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.data.Constants;
import com.angelp.purchasehistory.data.filters.PurchaseFilter;
import com.angelp.purchasehistory.data.interfaces.RefreshablePurchaseFragment;
import com.angelp.purchasehistory.data.model.DashboardComponent;
import com.angelp.purchasehistory.databinding.FragmentPurchasesListCardBinding;
import com.angelp.purchasehistory.ui.FullscreenGraphActivity;
import com.angelp.purchasehistory.ui.home.dashboard.purchases.PurchaseFilterDialog;
import com.angelp.purchasehistory.ui.home.dashboard.purchases.PurchasesAdapter;
import com.angelp.purchasehistory.web.clients.PurchaseClient;
import com.angelp.purchasehistorybackend.models.views.outgoing.PurchaseView;
import dagger.hilt.android.AndroidEntryPoint;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.util.List;

@AndroidEntryPoint
public class PurchaseListDashboardFragment extends RefreshablePurchaseFragment {
    private final String TAG = this.getClass().getSimpleName();
    private final PurchaseFilterDialog filterDialog = new PurchaseFilterDialog(true);
    private FragmentPurchasesListCardBinding binding;
    private PurchasesAdapter purchasesAdapter;
    @Inject
    PurchaseClient purchaseClient;
    private boolean showFilter;
    private int maxSize;

    public PurchaseListDashboardFragment() {
        Bundle args = new Bundle();
        this.setArguments(args);
    }

    @Override
    public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        maxSize = -1;
        if (getArguments() != null) {
            showFilter = getArguments().getBoolean(Constants.Arguments.ARG_SHOW_FILTER);
            maxSize = getArguments().getInt(Constants.Arguments.ARG_MAX_SIZE);
        }

        initializePurchasesRecyclerView(maxSize, filterViewModel.getFilterValue());
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPurchasesListCardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void initializePurchasesRecyclerView(int maxSize, PurchaseFilter filter) {
        new Thread(() -> {
            List<PurchaseView> purchases = purchaseClient.getAllPurchases(filter);
            purchasesAdapter = new PurchasesAdapter(purchases, getActivity());
            initFilterRow();
            setupShowMoreButton(purchases.size(), maxSize);
            applyFilter(filter);
            LinearLayoutManager llm = new LinearLayoutManager(getContext());
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            new Handler(Looper.getMainLooper()).post(() -> {
                if (binding != null) {
                    binding.purchaseList.setLayoutManager(llm);
                    binding.purchaseList.setAdapter(purchasesAdapter);
                }
            });
        }).start();
    }

    private void setupShowMoreButton(int purchaseSize, int maxSize) {
        binding.seeAllButton.setOnClickListener((v) -> {
            Intent intent = new Intent(getActivity(), FullscreenGraphActivity.class);
            DashboardComponent dashboardComponent = new DashboardComponent("PurchaseListPurchaseFragment");
            intent.putExtra(Constants.ARG_COMPONENT, dashboardComponent);
            startActivity(intent);
        });
        updateSeeAllButton(purchaseSize, maxSize);
    }

    private void updateSeeAllButton(int purchaseSize, int maxSize) {
        purchasesAdapter.setLimit(maxSize);
        boolean isBiggerThanLimit = maxSize > 0 && maxSize < purchaseSize;
        new Handler(Looper.getMainLooper()).post(() -> {
            binding.seeAllButton.setText(getString(R.string.see_all_n_purchases, purchaseSize));
            binding.seeAllButton.setVisibility(isBiggerThanLimit ? View.VISIBLE : View.GONE);
            binding.seeAllBackdrop.setVisibility(isBiggerThanLimit ? View.VISIBLE : View.GONE);
        });
    }

    public void refresh(PurchaseFilter filter) {
        if (purchasesAdapter == null) {
            Log.w(TAG, "refresh: Purchases adapter is missing. Skipping refresh");
            return;
        }
        new Thread(() -> {
            List<PurchaseView> allPurchases = purchaseClient.getAllPurchases(filter);
            Log.i(TAG, "Received purchases list with size of " + allPurchases.size());
            updateAdapter(allPurchases);
        }).start();
    }

    private void updateAdapter(List<PurchaseView> allPurchases) {
        new Handler(Looper.getMainLooper()).post(()->{
            purchasesAdapter.setPurchaseViews(allPurchases);
            updateSeeAllButton(allPurchases.size(), maxSize);
        });
    }

    private void initFilterRow() {
        binding.filterButton.setOnClickListener((v) -> openFilter());
        new Handler(Looper.getMainLooper()).post(() -> {
            binding.filterDateText.setTextColor(getContext().getColor(R.color.foreground_color));
            binding.filterRow.setVisibility(showFilter ? View.VISIBLE : View.GONE);
        });
    }

    private void applyFilter(PurchaseFilter newFilter) {
        filterViewModel.updateFilter(newFilter);
        new Handler(Looper.getMainLooper()).post(() -> {
            binding.filterButton.setText(R.string.filterButton);
            binding.filterDateText.setText(newFilter.getReadableString());
        });
    }

    private void openFilter() {
        filterDialog.show(getParentFragmentManager(), "barchartFilterDialog");
    }


}