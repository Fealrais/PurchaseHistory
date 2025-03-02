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
import com.angelp.purchasehistory.util.AndroidUtils;
import com.angelp.purchasehistory.web.clients.PurchaseClient;
import com.angelp.purchasehistorybackend.models.views.outgoing.PurchaseView;
import com.angelp.purchasehistorybackend.models.views.outgoing.analytics.CalendarReportEntry;
import dagger.hilt.android.AndroidEntryPoint;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.List;

@AndroidEntryPoint
public class PurchasesPerDayDialog extends DialogFragment {

    public static final String TOTAL_SUM = "total_sum";
    public static final String TITLE = "title";
    private final String TAG = PurchasesPerDayDialog.class.getSimpleName();
    private final DateTimeFormatter dtf_short = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
    private final DateTimeFormatter dtf_long = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG);

    private DialogGraphDetailsBinding binding;
    private PurchasesAdapter purchasesAdapter;
    @Inject
    PurchaseClient purchaseClient;
    private final int maxSize = 10;
    private AlertDialog dialog;
    private PurchaseFilter filter;

    public PurchasesPerDayDialog() {
        Bundle bundle = new Bundle();
        setArguments(bundle);
    }

    public PurchasesPerDayDialog(CalendarReportEntry calendarReportEntry) {
        Bundle bundle = new Bundle();
        PurchaseFilter purchaseFilter = new PurchaseFilter(calendarReportEntry.getLocalDate());
        bundle.putParcelable(Constants.Arguments.ARG_FILTER, purchaseFilter);
        bundle.putString(TITLE, calendarReportEntry.getLocalDate().format(dtf_long));
        bundle.putFloat(TOTAL_SUM, calendarReportEntry.getSum().floatValue());
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
        builder.setMessage(AndroidUtils.formatCurrency(getArguments().getFloat(TOTAL_SUM), getContext()));
        builder.setView(binding.getRoot());
        filter = getArguments().getParcelable(Constants.Arguments.ARG_FILTER);
        initializePurchasesRecyclerView(maxSize, filter);
        updateFilterButtons(filter.getFrom());
        binding.nextDayButton.setOnClickListener((v) -> updateFilter(filter.getFrom().plusDays(1)));
        binding.previousDayButton.setOnClickListener((v) -> updateFilter(filter.getFrom().minusDays(1)));
        dialog = builder.create();
        return dialog;
    }

    private void updateFilter(LocalDate localDate) {
        filter.setFrom(localDate);
        filter.setTo(localDate);
        updateFilterButtons(localDate);
        refresh(filter);
    }

    private void updateFilterButtons(LocalDate localDate) {
        binding.nextDayButton.setText(localDate.plusDays(1).format(dtf_short));
        binding.previousDayButton.setText(localDate.minusDays(1).format(dtf_short));
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
            List<PurchaseView> allPurchases = purchaseClient.getAllPurchases(filter);
            purchasesAdapter.getPurchaseViews().clear();
            Log.i(TAG, "Received purchases list with size of " + allPurchases.size());
            updateUI(allPurchases);
        }).start();
    }

    private void updateUI(List<PurchaseView> allPurchases) {
        BigDecimal sum = allPurchases.stream().reduce(BigDecimal.ZERO, (bigDecimal, view) -> bigDecimal.add(view.getPrice()), BigDecimal::add);

        new Handler(Looper.getMainLooper()).post(() -> {
            dialog.setTitle(filter.getFrom().format(dtf_long));
            dialog.setMessage(AndroidUtils.formatCurrency(sum, getContext()));
            purchasesAdapter.setPurchaseViews(allPurchases);
            updateSeeAllButton(allPurchases.size(), maxSize);
        });
    }
}
