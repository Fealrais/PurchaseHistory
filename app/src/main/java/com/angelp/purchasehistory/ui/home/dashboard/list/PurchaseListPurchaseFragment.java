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
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.data.Constants;
import com.angelp.purchasehistory.data.filters.PurchaseFilter;
import com.angelp.purchasehistory.data.model.DashboardComponent;
import com.angelp.purchasehistory.databinding.FragmentPurchasesListCardBinding;
import com.angelp.purchasehistory.ui.FullscreenGraphActivity;
import com.angelp.purchasehistory.ui.home.dashboard.RefreshableFragment;
import com.angelp.purchasehistory.ui.home.dashboard.purchases.PurchasesAdapter;
import com.angelp.purchasehistory.ui.home.dashboard.purchases.PurchasesViewModel;
import com.angelp.purchasehistorybackend.models.views.outgoing.PurchaseView;
import dagger.hilt.android.AndroidEntryPoint;

import java.util.List;
import java.util.function.Consumer;

@AndroidEntryPoint
public class PurchaseListPurchaseFragment extends RefreshableFragment {
    private final String TAG = this.getClass().getSimpleName();
    private FragmentPurchasesListCardBinding binding;
    private PurchasesViewModel purchasesViewModel;
    private PurchasesAdapter purchasesAdapter;

    public PurchaseListPurchaseFragment(PurchaseFilter filter, Consumer<PurchaseFilter> onFilterChange) {
        super(filter, onFilterChange);
        Bundle args = new Bundle();
        args.putParcelable(Constants.ARG_FILTER, filter);
        this.setArguments(args);
    }


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPurchasesListCardBinding.inflate(inflater, container, false);
        int maxSize = -1;
        if (getArguments() != null) {
            filter = getArguments().getParcelable(Constants.ARG_FILTER);
            maxSize = getArguments().getInt(Constants.ARG_MAX_SIZE);
        }
        purchasesViewModel = new ViewModelProvider(this).get(PurchasesViewModel.class);
        initializePurchasesRecyclerView(maxSize);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void initializePurchasesRecyclerView(int maxSize) {
        new Thread(() -> {
            List<PurchaseView> purchases = purchasesViewModel.getPurchaseClient().getAllPurchases(filter);
            purchasesAdapter = new PurchasesAdapter(purchases, getActivity(), () -> setFilter.accept(filter));
            setupShowMoreButton(purchases.size(), maxSize);
            LinearLayoutManager llm = new LinearLayoutManager(getContext());
            llm.setOrientation(LinearLayoutManager.VERTICAL);
            new Handler(Looper.getMainLooper()).post(() -> {
                if (binding != null) {
                    binding.purchaseList.setLayoutManager(llm);
                    binding.purchaseList.setItemAnimator(new DefaultItemAnimator());
                    binding.purchaseList.setAdapter(purchasesAdapter);
                }
            });
        }).start();
    }

    private void setupShowMoreButton(int purchaseSize, int maxSize) {
        boolean isBiggerThanLimit = maxSize > 0 && maxSize < purchaseSize;
        if (isBiggerThanLimit) {
            purchasesAdapter.setLimit(maxSize);
            binding.seeAllButton.setOnClickListener((v) -> {
                Intent intent = new Intent(getActivity(), FullscreenGraphActivity.class);
                DashboardComponent dashboardComponent = new DashboardComponent("PurchaseListPurchaseFragment");
                intent.putExtra("component", dashboardComponent);
                startActivity(intent);
            });
            binding.seeAllButton.setText(getString(R.string.see_all_n_purchases, purchaseSize));
        }
        new Handler(Looper.getMainLooper()).post(()->{
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
            purchasesAdapter.getPurchaseViews().clear();
            List<PurchaseView> allPurchases = purchasesViewModel.getPurchaseClient().getAllPurchases(filter);
            Log.i(TAG, "Received purchases list with size of " + allPurchases.size());
            purchasesAdapter.setPurchaseViews(allPurchases);
            getActivity().runOnUiThread(() -> purchasesAdapter.notifyDataSetChanged());
            Log.i(TAG, "Adapter notified");
        }).start();
    }


}