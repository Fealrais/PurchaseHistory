package com.example.purchasehistory.ui.home.dashboard.list;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.angelp.purchasehistorybackend.models.views.outgoing.PurchaseView;
import com.example.purchasehistory.data.filters.PurchaseFilter;
import com.example.purchasehistory.data.interfaces.RefreshablePurchaseFragment;
import com.example.purchasehistory.databinding.FragmentPurchasesListCardBinding;
import com.example.purchasehistory.ui.home.purchases.PurchasesAdapter;
import com.example.purchasehistory.ui.home.purchases.PurchasesViewModel;
import dagger.hilt.android.AndroidEntryPoint;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AndroidEntryPoint
public class PurchaseListPurchaseFragment extends Fragment implements RefreshablePurchaseFragment {
    private static final String ARG_FILTER = "purchase_filter";
    private final String TAG = this.getClass().getSimpleName();
    private FragmentPurchasesListCardBinding binding;
    private PurchasesViewModel purchasesViewModel;
    private PurchasesAdapter purchasesAdapter;
    @Setter
    private PurchaseFilter filter;
    private Runnable refreshDashboard;

    public PurchaseListPurchaseFragment(PurchaseFilter filter, Runnable refresh) {
        this.filter = filter;
        Bundle args = new Bundle();
        args.putParcelable(ARG_FILTER, filter);
        this.setArguments(args);
        this.refreshDashboard = refresh;
    }


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPurchasesListCardBinding.inflate(inflater, container, false);
        if (getArguments() != null) {
            filter = getArguments().getParcelable(ARG_FILTER);
        }
        purchasesViewModel = new ViewModelProvider(this).get(PurchasesViewModel.class);
        initializePurchasesRecyclerView();
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void initializePurchasesRecyclerView() {
        new Thread(() -> {
            List<PurchaseView> purchases = purchasesViewModel.getPurchaseClient().getAllPurchases(filter);
            purchasesAdapter = new PurchasesAdapter(purchases, getActivity(), refreshDashboard);
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