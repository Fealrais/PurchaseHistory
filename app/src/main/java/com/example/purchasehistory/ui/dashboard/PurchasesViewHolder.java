package com.example.purchasehistory.ui.dashboard;

import android.view.View;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.purchasehistory.databinding.RecyclerViewPurchaseBinding;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

@Getter
public class PurchasesViewHolder extends RecyclerView.ViewHolder{
    RecyclerViewPurchaseBinding binding;
    public PurchasesViewHolder(@NonNull @NotNull View itemView) {
        super(itemView);
        binding = RecyclerViewPurchaseBinding.bind(itemView);
    }
}
