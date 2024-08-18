package com.example.purchasehistory.ui.home.purchases;

import android.os.Bundle;
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
import com.angelp.purchasehistorybackend.models.views.outgoing.CategoryView;
import com.example.purchasehistory.R;
import com.example.purchasehistory.components.form.DatePickerFragment;
import com.example.purchasehistory.data.filters.PurchaseFilter;
import com.example.purchasehistory.databinding.FragmentPurchaseFilterDialogBinding;
import com.example.purchasehistory.web.clients.PurchaseClient;
import dagger.hilt.android.AndroidEntryPoint;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Getter
@Setter
@AndroidEntryPoint
public class PurchaseFilterDialog extends DialogFragment {
    private final String TAG = this.getClass().getSimpleName();

    @Inject
    PurchaseClient purchaseClient;
    private FragmentPurchaseFilterDialogBinding binding;
    private DatePickerFragment datePickerFrom;
    private DatePickerFragment datePickerTo;
    private ArrayAdapter<CategoryView> categoryAdapter;
    private PurchaseFilter filter;
    private List<CategoryView> categoryOptions = new ArrayList<>();
    private Consumer<PurchaseFilter> onSuccess;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.i(getTag(), "onCreateView: View created");
        binding = FragmentPurchaseFilterDialogBinding.inflate(inflater, container, false);
        datePickerFrom = new DatePickerFragment();
        datePickerTo = new DatePickerFragment();

        getParentFragmentManager().setFragmentResultListener("categoryResult", getViewLifecycleOwner(), (requestKey, result) -> {
            CategoryView newCategoryView;
            newCategoryView = result.getParcelable("newCategoryView");
            if (newCategoryView != null) categoryAdapter.add(newCategoryView);
        });
        datePickerFrom.getDateResult().observe(getViewLifecycleOwner(), (v) -> {
            filter.setFrom(v);
            binding.purchaseFilterFromDate.setText(v.format(DateTimeFormatter.ISO_LOCAL_DATE));
        });
        datePickerTo.getDateResult().observe(getViewLifecycleOwner(), (v) -> {
            filter.setTo(v);
            binding.purchaseFilterToDate.setText(v.format(DateTimeFormatter.ISO_LOCAL_DATE));
        });
        binding.purchaseFilterClearButton.setOnClickListener(v -> {
            if (getActivity() != null)
                getActivity().runOnUiThread(this::resetForm);
        });
        binding.purchaseFilterFilterButton.setOnClickListener((view) -> onSuccess.accept(filter));
        binding.purchaseFilterFromDate.setOnClickListener((v) -> datePickerFrom.show(getParentFragmentManager(), "datePickerFrom"));
        binding.purchaseFilterToDate.setOnClickListener((v) -> datePickerTo.show(getParentFragmentManager(), "datePickerTo"));
        new Thread(() -> {
            categoryOptions = purchaseClient.getAllCategories();
            categoryOptions.add(0, new CategoryView(null, "None", "#fff"));
            categoryAdapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_spinner_item, categoryOptions);
            getActivity().runOnUiThread(() -> binding.purchaseFilterCategorySpinner.setAdapter(categoryAdapter));
        }).start();
        binding.purchaseFilterCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                CategoryView categoryView = categoryOptions.get(position);
                filter.setCategoryId(categoryView.getId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                filter.setCategoryId(null);
            }
        });
        fillEditForm(filter);
        return binding.getRoot();
    }

    private void fillEditForm(PurchaseFilter view) {
        new Thread(() -> {
            if (view.getFrom() != null) {
                datePickerFrom.getDateResult().postValue(view.getFrom());
            }
            if (view.getTo() != null) {
                datePickerTo.getDateResult().postValue(view.getTo());
            }
            if (view.getCategoryId() != null) {
                for (int i = 0; i < categoryOptions.size(); i++) {
                    if (view.getCategoryId().equals(categoryOptions.get(i).getId())) {
                        binding.purchaseFilterCategorySpinner.setSelection(i);
                        break;
                    }
                }
            }
        }).start();

    }

    private void resetForm() {
        this.filter = new PurchaseFilter();
        binding.purchaseFilterCategorySpinner.setSelection(0);
        binding.purchaseFilterToDate.setText(R.string.to);
        binding.purchaseFilterFromDate.setText(R.string.from);
        onSuccess.accept(filter);
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
