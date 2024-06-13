package com.example.purchasehistory.ui.dashboard;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;
import com.angelp.purchasehistorybackend.models.views.incoming.PurchaseDTO;
import com.angelp.purchasehistorybackend.models.views.outgoing.PurchaseView;
import com.example.purchasehistory.PurchaseHistoryApplication;
import com.example.purchasehistory.R;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.example.purchasehistory.data.Constants.PURCHASE_EDIT_DIALOG_ID_KEY;

public class PurchasesAdapter extends RecyclerView.Adapter<PurchasesViewHolder> {
    @Getter
    private final List<PurchaseView> purchaseViews;
    private final FragmentActivity fragmentActivity;
    private final DateTimeFormatter readableFormatter = DateTimeFormatter.ofPattern("dd.MM.yy hh:mm:ss");
    private PurchaseEditDialog editDialog;


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
        return new PurchasesViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull PurchasesViewHolder holder, int position) {
        PurchaseView purchaseView = purchaseViews.get(position);
        editDialog = new PurchaseEditDialog();
        if (purchaseView.getCreatedDate() != null) {
            holder.getBinding().purchaseCreatedDate.setText(purchaseView.getCreatedDate().format(readableFormatter));
        }
        if (purchaseView.getPrice() != null)
            holder.getBinding().purchasePriceText.setText(String.format(purchaseView.getPrice().toString()));
        if (purchaseView.getTimestamp() != null)
            holder.getBinding().purchaseTimeText.setText(purchaseView.getTimestamp().format(readableFormatter));
        if (purchaseView.getCategory() != null)
            holder.getBinding().bgImage.setColorFilter(Color.parseColor(purchaseView.getCategory().getColor().toUpperCase()));
        else holder.getBinding().bgImage.clearColorFilter();
        if (purchaseView.getTimestamp() != null)
            holder.getBinding().purchaseEditButton.setOnClickListener((v) -> {
                if (purchaseView.getId() != null) {
                    PurchaseDTO purchaseDTO = generatePurchaseDTO(purchaseView);
                    editDialog.setPurchase(purchaseDTO);
                    Bundle bundle = new Bundle();
                    bundle.putLong(PURCHASE_EDIT_DIALOG_ID_KEY, purchaseView.getId());
                    editDialog.setArguments(bundle);
                    editDialog.show(fragmentActivity.getSupportFragmentManager(), "editDialog" + purchaseView.getBillId());
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


    @Override
    public int getItemCount() {
        if (purchaseViews == null) return 0;
        return purchaseViews.size();
    }
}
