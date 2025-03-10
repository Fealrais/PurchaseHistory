package com.angelp.purchasehistory.ui.home.dashboard.purchases;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.angelp.purchasehistory.databinding.FragmentPurchaseEditDialogBinding;
import com.angelp.purchasehistory.web.clients.PurchaseClient;
import com.angelp.purchasehistorybackend.models.views.incoming.PurchaseDTO;
import dagger.hilt.android.lifecycle.HiltViewModel;
import lombok.Getter;
import lombok.Setter;

import javax.inject.Inject;

@Getter
@HiltViewModel
public class EditPurchaseViewModel extends ViewModel {
    private final PurchaseClient purchaseClient;
    private final MutableLiveData<PurchaseDTO> purchase = new MutableLiveData<>();
    @Setter
    private FragmentPurchaseEditDialogBinding binding;

    @Inject
    public EditPurchaseViewModel(PurchaseClient purchaseClient) {
        this.purchaseClient = purchaseClient;
    }

}
