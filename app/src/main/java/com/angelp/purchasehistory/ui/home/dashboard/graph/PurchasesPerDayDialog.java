package com.angelp.purchasehistory.ui.home.dashboard.graph;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.data.Constants;
import com.angelp.purchasehistory.data.filters.PurchaseFilter;
import com.angelp.purchasehistory.data.model.DashboardComponent;
import com.angelp.purchasehistory.databinding.DialogGraphDetailsBinding;
import com.angelp.purchasehistory.ui.FullscreenGraphActivity;
import com.angelp.purchasehistory.ui.home.dashboard.purchases.PurchasesAdapter;
import com.angelp.purchasehistory.web.clients.PurchaseClient;
import com.angelp.purchasehistorybackend.models.views.outgoing.PurchaseView;
import com.angelp.purchasehistorybackend.models.views.outgoing.analytics.CalendarReportEntry;
import dagger.hilt.android.AndroidEntryPoint;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@AndroidEntryPoint
public class PurchasesPerDayDialog extends DialogFragment {

    public static final String TOTAL_SUM = "total_sum";
    public static final String TITLE = "title";
    private final String TAG = PurchasesPerDayDialog.class.getSimpleName();
    private DialogGraphDetailsBinding binding;
    private PurchasesAdapter purchasesAdapter;
    @Inject
    PurchaseClient purchaseClient;
    private final int maxSize = 10;
    private AlertDialog dialog;

    public PurchasesPerDayDialog() {
        Bundle bundle = new Bundle();
        setArguments(bundle);
    }

    public PurchasesPerDayDialog(CalendarReportEntry calendarReportEntry) {
        Bundle bundle = new Bundle();
        PurchaseFilter purchaseFilter = new PurchaseFilter(calendarReportEntry.getLocalDate());
        bundle.putParcelable(Constants.Arguments.ARG_FILTER, purchaseFilter);
        bundle.putString(TITLE, calendarReportEntry.getLocalDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
        bundle.putString(TOTAL_SUM, calendarReportEntry.getSum().toString());
        setArguments(bundle);
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        //Set all the title, button etc. for the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater layoutInflater = getLayoutInflater();
        binding = DialogGraphDetailsBinding.inflate(layoutInflater, null, false);
        builder.setTitle(getArguments().getString(TITLE));
        builder.setMessage(getArguments().getString(TOTAL_SUM));
        builder.setView(binding.getRoot());
        PurchaseFilter filter = getArguments().getParcelable(Constants.Arguments.ARG_FILTER);
        initializePurchasesRecyclerView(maxSize, filter);
        dialog = builder.create();
        return dialog;
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
            BigDecimal sum = purchases.stream().map(PurchaseView::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
            dialog.setMessage(getString(R.string.currency_total, sum.floatValue()));
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
        if (purchasesAdapter == null || binding == null) {
            Log.w(TAG, "refresh: Purchases adapter is missing. Skipping refresh");
            return;
        }
        new Thread(() -> {
            purchasesAdapter.getPurchaseViews().clear();
            List<PurchaseView> allPurchases = purchaseClient.getAllPurchases(filter);
            Log.i(TAG, "Received purchases list with size of " + allPurchases.size());
            updateAdapter(allPurchases);
        }).start();
    }

    private void updateAdapter(List<PurchaseView> allPurchases) {
        new Handler(Looper.getMainLooper()).post(() -> {
            purchasesAdapter.setPurchaseViews(allPurchases);
            updateSeeAllButton(allPurchases.size(), maxSize);
        });
    }
}
