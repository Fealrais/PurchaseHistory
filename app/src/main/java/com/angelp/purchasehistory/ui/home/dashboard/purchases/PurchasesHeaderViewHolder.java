package com.angelp.purchasehistory.ui.home.dashboard.purchases;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import com.angelp.purchasehistory.data.interfaces.ViewHolder;
import com.angelp.purchasehistory.databinding.RecyclerViewPurchaseHeaderBinding;
import com.angelp.purchasehistorybackend.models.views.outgoing.PurchaseView;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public class PurchasesHeaderViewHolder extends ViewHolder<PurchaseView> {
    private final String TAG = this.getClass().getSimpleName();

    final RecyclerViewPurchaseHeaderBinding binding;
    private FragmentManager fragmentManager;


    public PurchasesHeaderViewHolder(@NonNull @NotNull View itemView) {
        super(itemView);
        binding = RecyclerViewPurchaseHeaderBinding.bind(itemView);
    }

    public void bind(PurchaseView purchaseView, FragmentManager fragmentManager) {
        this.fragmentManager = fragmentManager;
        PurchaseViewHeader header = (PurchaseViewHeader) purchaseView;
        binding.purchaseHeaderTimeText.setText(header.getTitle());
    }

}
