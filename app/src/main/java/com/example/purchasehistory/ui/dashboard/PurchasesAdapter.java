package com.example.purchasehistory.ui.dashboard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import com.angelp.purchasehistorybackend.models.views.outgoing.PurchaseView;
import com.example.purchasehistory.R;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PurchasesAdapter extends RecyclerView.Adapter<PurchasesViewHolder> {
    private DashboardViewModel dashboardViewModel;
    @Getter
    private final List<PurchaseView> purchaseViews;
    private final FragmentActivity fragmentActivity;

    public PurchasesAdapter(List<PurchaseView> purchaseViews, FragmentActivity fragmentActivity) {
        this.purchaseViews = purchaseViews;
        this.fragmentActivity = fragmentActivity;
    }

    @NonNull
    @NotNull
    @Override
    public PurchasesViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recycler_view_purchase, parent, false);
        dashboardViewModel = new ViewModelProvider(fragmentActivity).get(DashboardViewModel.class);
        return new PurchasesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull PurchasesViewHolder holder, int position) {
        PurchaseView purchaseView = purchaseViews.get(position);

        if (purchaseView.getCreatedDate() != null)
            holder.getBinding().purchaseCreatedDate.setText(String.format("Created: %s",purchaseView.getCreatedDate()));
        if (purchaseView.getPrice() != null)
            holder.getBinding().purchasePriceText.setText(String.format(purchaseView.getPrice().toString()));
        if (purchaseView.getTimestamp() != null)
            holder.getBinding().purchaseTimeText.setText(purchaseView.getTimestamp().toString());
    }


    @Override
    public int getItemCount() {
        if (purchaseViews == null) return 0;
        return purchaseViews.size();
    }
}
