package com.example.purchasehistory.ui.home.purchases;

import android.graphics.Color;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import com.angelp.purchasehistorybackend.models.views.incoming.PurchaseDTO;
import com.angelp.purchasehistorybackend.models.views.outgoing.PurchaseView;
import com.example.purchasehistory.PurchaseHistoryApplication;
import com.example.purchasehistory.data.interfaces.ViewHolder;
import com.example.purchasehistory.databinding.RecyclerViewPurchaseBinding;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import static com.example.purchasehistory.data.Constants.PURCHASE_EDIT_DIALOG_ID_KEY;

@Getter
public class PurchasesViewHolder extends ViewHolder<PurchaseView> {
    private final String TAG = this.getClass().getSimpleName();
    private final DateTimeFormatter readableFormatter = DateTimeFormatter.ofPattern("dd.MM.yy hh:mm:ss");

    RecyclerViewPurchaseBinding binding;
    private PurchaseEditDialog editDialog;
    private FragmentManager fragmentManager;

    public PurchasesViewHolder(@NonNull @NotNull View itemView) {
        super(itemView);
        binding = RecyclerViewPurchaseBinding.bind(itemView);
    }

    public void bind(PurchaseView purchaseView, FragmentManager fragmentManager) {
        editDialog = new PurchaseEditDialog();
        this.fragmentManager = fragmentManager;
        if (purchaseView.getPrice() != null)
            binding.purchasePriceText.setText(String.format(Locale.ENGLISH, "%.2f", purchaseView.getPrice().doubleValue()));
        if (purchaseView.getTimestamp() != null) {
            long epochMilli = purchaseView.getTimestamp().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            CharSequence timeString = DateUtils.getRelativeTimeSpanString(epochMilli);
            binding.purchaseTimeText.setText(timeString);
        }
        if (purchaseView.getCategory() != null) {
            binding.purchaseCategoryText.setVisibility(View.VISIBLE);
            binding.purchaseCategoryText.setText(purchaseView.getCategory().getName().toUpperCase());
            binding.purchaseCategoryText.setBackgroundColor(Color.parseColor(purchaseView.getCategory().getColor().toUpperCase()));
        } else {
            binding.purchaseCategoryText.setVisibility(View.INVISIBLE);
        }
        if (purchaseView.getTimestamp() != null)
            binding.purchaseEditButton.setOnClickListener((v) -> {
                if (purchaseView.getId() != null) {
                    PurchaseDTO purchaseDTO = generatePurchaseDTO(purchaseView);
                    editDialog.setPurchase(purchaseDTO);
                    Bundle bundle = new Bundle();
                    bundle.putLong(PURCHASE_EDIT_DIALOG_ID_KEY, purchaseView.getId());
                    editDialog.setArguments(bundle);
                    editDialog.show(fragmentManager, "editDialog" + purchaseView.getBillId());
                    editDialog.setOnSuccess((newView) -> this.bind(newView, fragmentManager));
                } else PurchaseHistoryApplication.getInstance().alert("Purchase does not have an id");

            });
    }

    private PurchaseDTO generatePurchaseDTO(PurchaseView purchaseView) {
        PurchaseDTO purchaseDTO = new PurchaseDTO();
        purchaseDTO.setQrContent(purchaseView.getQrContent());
        purchaseDTO.setPrice(purchaseView.getPrice());
        purchaseDTO.setTimestamp(purchaseView.getTimestamp());
        purchaseDTO.setBillId(purchaseView.getBillId());
        purchaseDTO.setStoreId(purchaseView.getStoreId());
        if (purchaseView.getCategory() != null) purchaseDTO.setCategoryId(purchaseView.getCategory().getId());
        return purchaseDTO;
    }

}
