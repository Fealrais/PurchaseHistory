package com.angelp.purchasehistory.ui.home.purchases;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.angelp.purchasehistorybackend.models.views.outgoing.PurchaseView;
import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.data.interfaces.ViewHolder;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PurchasesAdapter extends RecyclerView.Adapter<ViewHolder<PurchaseView>> {


    @Getter
    private final List<PurchaseView> purchaseViews = new ArrayList<>();
    private final FragmentActivity fragmentActivity;
    private final Runnable refreshDashboard;


    public PurchasesAdapter(List<PurchaseView> purchaseViews, FragmentActivity fragmentActivity, Runnable refreshDashboard) {
        setPurchaseViews(purchaseViews);
        this.fragmentActivity = fragmentActivity;
        this.refreshDashboard = refreshDashboard;
    }

    public void setPurchaseViews(List<PurchaseView> purchaseViews) {
        this.purchaseViews.clear();
        this.purchaseViews.addAll(separateByMonth(purchaseViews));
    }

    private List<PurchaseView> separateByMonth(List<PurchaseView> purchaseViews) {
        List<PurchaseView> result = new ArrayList<>();
        if (purchaseViews == null || purchaseViews.isEmpty()) return result;
        LocalDate currentMonth = purchaseViews.get(0).getTimestamp().toLocalDate().withDayOfMonth(1);
        result.add(new PurchaseViewHeader(purchaseViews.get(0)));
        for (PurchaseView purchase : purchaseViews) {
            if (!currentMonth.equals(purchase.getTimestamp().toLocalDate().withDayOfMonth(1))) {
                result.add(new PurchaseViewHeader(purchase));
                currentMonth = purchase.getTimestamp().toLocalDate().withDayOfMonth(1);
            }
            result.add(purchase);
        }
        return result;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder<PurchaseView> onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case PurchaseViewHeader.TYPE_HEADER: {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_purchase_header, parent, false);
                return new PurchasesHeaderViewHolder(view);
            }
            case PurchaseViewHeader.TYPE_PURCHASE: {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.recycler_view_purchase, parent, false);
                return new PurchasesViewHolder(view);
            }
            default:
                throw new IllegalStateException("unsupported item type");
        }

    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder<PurchaseView> holder, int position) {
        if (purchaseViews.size() <= position) return;
        PurchaseView purchaseView = purchaseViews.get(position);
        holder.bind(purchaseView, fragmentActivity.getSupportFragmentManager(), refreshDashboard);
    }
    private void removePurchase(int index) {
        this.purchaseViews.remove(index);
        new Handler(Looper.getMainLooper()).post(() -> notifyItemRemoved(index));
    }

    @Override
    public int getItemCount() {
        return purchaseViews.size();
    }

    @Override
    public int getItemViewType(int position) {
        return PurchaseViewHeader.isHeader(purchaseViews.get(position)) ? PurchaseViewHeader.TYPE_HEADER : PurchaseViewHeader.TYPE_PURCHASE;
    }
}
