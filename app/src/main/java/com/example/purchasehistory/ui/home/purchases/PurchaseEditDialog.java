package com.example.purchasehistory.ui.home.purchases;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import com.angelp.purchasehistorybackend.models.views.incoming.PurchaseDTO;
import com.angelp.purchasehistorybackend.models.views.outgoing.CategoryView;
import com.angelp.purchasehistorybackend.models.views.outgoing.PurchaseView;
import com.example.purchasehistory.PurchaseHistoryApplication;
import com.example.purchasehistory.R;
import com.example.purchasehistory.components.form.CreateCategoryDialog;
import com.example.purchasehistory.components.form.DatePickerFragment;
import com.example.purchasehistory.components.form.TimePickerFragment;
import com.example.purchasehistory.databinding.FragmentPurchaseEditDialogBinding;
import com.example.purchasehistory.util.CommonUtils;
import com.example.purchasehistory.web.clients.PurchaseClient;
import dagger.hilt.android.AndroidEntryPoint;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import static com.example.purchasehistory.data.Constants.PURCHASE_EDIT_DIALOG_ID_KEY;

@Getter
@Setter
@AndroidEntryPoint
public class PurchaseEditDialog extends DialogFragment {
    private final String TAG = this.getClass().getSimpleName();

    @Inject
    PurchaseClient purchaseClient;
    private PurchaseDTO purchase;
    private FragmentPurchaseEditDialogBinding binding;
    private TimePickerFragment timePicker;
    private DatePickerFragment datePicker;
    private CreateCategoryDialog categoryDialog;
    private ArrayAdapter<CategoryView> categoryAdapter;
    private List<CategoryView> allCategories = new ArrayList<>();
    private Consumer<PurchaseView> onSuccess;
    private Long purchaseId;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.i(getTag(), "onCreateView: View created");
        binding = FragmentPurchaseEditDialogBinding.inflate(inflater, container, false);
        Bundle bundle = getArguments();
        if (bundle != null) {
            this.purchaseId = bundle.getLong(PURCHASE_EDIT_DIALOG_ID_KEY, 0L);
            binding.purchaseEditUpdatePurchaseText.setText(String.format(Locale.getDefault(), "Update purchase #%d", purchaseId));
        }
        timePicker = new TimePickerFragment(purchase.getTime());
        datePicker = new DatePickerFragment(purchase.getDate());
        categoryDialog = new CreateCategoryDialog((newCategory) -> new Handler(Looper.getMainLooper()).post(() -> {
            if (newCategory != null) {
                categoryAdapter.add(newCategory);
                binding.purchaseEditCategorySpinner.setSelection(categoryAdapter.getPosition(newCategory));
            }
        }));

        timePicker.getTimeResult().observe(getViewLifecycleOwner(), (v) -> {
            purchase.setTime(v);
            binding.purchaseEditTimeInput.setText(v.format(DateTimeFormatter.ISO_LOCAL_TIME));
        });
        datePicker.getDateResult().observe(getViewLifecycleOwner(), (v) -> {
            purchase.setDate(v);
            binding.purchaseEditDateInput.setText(v.format(DateTimeFormatter.ISO_LOCAL_DATE));
        });
        binding.purchaseEditClearButton.setOnClickListener(v -> {
            if (getActivity() != null)
                getActivity().runOnUiThread(this::resetForm);
        });
        binding.purchaseEditSaveButton.setOnClickListener((view) -> {
            Log.i(getTag(), "Edit button clicked");
            onSubmit(purchase, purchaseId);
        });
        binding.purchaseEditTimeInput.setOnClickListener((v) -> timePicker.show(getParentFragmentManager(), "timePicker"));
        binding.purchaseEditDateInput.setOnClickListener((v) -> datePicker.show(getParentFragmentManager(), "datePicker"));
        binding.purchaseEditCategoryAddButton.setOnClickListener((v) -> categoryDialog.show(getParentFragmentManager(), "createCategoryDialog"));
        new Thread(() -> {
            allCategories = purchaseClient.getAllCategories();
            categoryAdapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_spinner_item, allCategories);
            getActivity().runOnUiThread(() -> {
                binding.purchaseEditCategorySpinner.setAdapter(categoryAdapter);
                int index = CommonUtils.findIndex(allCategories, (category) -> category.getId().equals(purchase.getCategoryId()));
                if (index >= 0)
                    binding.purchaseEditCategorySpinner.setSelection(index);
            });
        }).start();
        binding.purchaseEditCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                CategoryView categoryView = allCategories.get(position);
                purchase.setCategoryId(categoryView.getId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                purchase.setCategoryId(null);
            }
        });
        fillEditForm(purchase);
        return binding.getRoot();
    }

    private void fillEditForm(PurchaseDTO view) {
        new Thread(() -> {
            if (view.getPrice() != null)
                binding.purchaseEditPriceInput.setText(String.format(view.getPrice().toString()));
            if (view.getTimestamp() != null) {
                timePicker.setValue(view.getTime());
                datePicker.setValue(view.getDate());
            }
            if (view.getBillId() != null)
                binding.purchaseEditBillIdValue.setText(view.getBillId());
            if (view.getStoreId() != null)
                binding.purchaseEditStoreIdValue.setText(view.getStoreId());
            if (view.getCategoryId() != null) {
                for (int i = 0; i < allCategories.size(); i++) {
                    if (view.getCategoryId().equals(allCategories.get(i).getId())) {
                        binding.purchaseEditCategorySpinner.setSelection(i);
                        break;
                    }
                }
            }
        }).start();

    }


    private void onSubmit(PurchaseDTO data, Long id) {
        if (id != null) {
            new Thread(() -> {
                PurchaseView purchaseView = purchaseClient.editPurchase(data, id);
                if (purchaseView != null) {
                    PurchaseHistoryApplication.getInstance().alert("Updated purchase #" + purchaseView.getBillId() + ". Cost:" + purchaseView.getPrice());
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            resetForm();
                            if (onSuccess != null) onSuccess.accept(purchaseView);
                        });
                    }
                    dismiss();
                } else
                    PurchaseHistoryApplication.getInstance().alert("Failed to register purchase #");
            }).start();
        }
    }

    private void resetForm() {
        this.purchase = new PurchaseDTO();
        this.purchaseId = null;
        binding.purchaseEditCategorySpinner.setSelection(0);
        binding.purchaseEditPriceInput.getText().clear();
        binding.purchaseEditDateInput.setText(R.string.date);
        binding.purchaseEditTimeInput.setText(R.string.time);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.dismiss();
    }

    @Override
    public void show(@NonNull @NotNull FragmentManager manager, @Nullable String tag) {
        if (this.isAdded()) {
            Log.w(TAG, "Fragment already added");
            return;
        }
        super.show(manager, tag);
    }
}
