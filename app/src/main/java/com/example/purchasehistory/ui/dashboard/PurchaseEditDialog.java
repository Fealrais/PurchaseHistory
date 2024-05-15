package com.example.purchasehistory.ui.dashboard;

import androidx.fragment.app.DialogFragment;
import com.angelp.purchasehistorybackend.models.views.outgoing.PurchaseView;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PurchaseEditDialog extends DialogFragment {

    private PurchaseView purchase;

}
