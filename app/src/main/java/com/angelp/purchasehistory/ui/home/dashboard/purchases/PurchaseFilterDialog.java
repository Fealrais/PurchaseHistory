package com.angelp.purchasehistory.ui.home.dashboard.purchases;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.components.form.DatePickerFragment;
import com.angelp.purchasehistory.data.filters.CategoryFilter;
import com.angelp.purchasehistory.data.filters.PurchaseFilter;
import com.angelp.purchasehistory.data.filters.PurchaseFilterSingleton;
import com.angelp.purchasehistory.databinding.FragmentPurchaseFilterDialogBinding;
import com.angelp.purchasehistory.web.clients.PurchaseClient;
import com.angelp.purchasehistorybackend.models.views.outgoing.CategoryView;
import dagger.hilt.android.AndroidEntryPoint;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.angelp.purchasehistory.data.Constants.getDefaultFilter;

@Getter
@Setter
@AndroidEntryPoint
public class PurchaseFilterDialog extends DialogFragment {
    private final String TAG = this.getClass().getSimpleName();
    private final boolean containCategory;

    @Inject
    PurchaseClient purchaseClient;
    @Inject
    PurchaseFilterSingleton filterViewModel;

    private FragmentPurchaseFilterDialogBinding binding;
    private DatePickerFragment datePickerFrom;
    private DatePickerFragment datePickerTo;
    private ArrayAdapter<CategoryView> categoryAdapter;
    private PurchaseFilter filter;
    private List<CategoryView> categoryOptions = new ArrayList<>();


    public PurchaseFilterDialog(boolean containCategory) {
        this.containCategory = containCategory;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.i(getTag(), "onCreateView: View created");
        binding = FragmentPurchaseFilterDialogBinding.inflate(inflater, container, false);
        filterViewModel.getFilter().observe(getViewLifecycleOwner(), this::updateFilter);
        updateFilter(filterViewModel.getFilterValue());
        setupDatePickers();
        setupCategorySpinner(containCategory);
        fillEditForm(filter);
        binding.purchaseFilterSubmitButton.setOnClickListener((view) -> {
            filterViewModel.updateFilter(filter);
            this.dismiss();
        });
        return binding.getRoot();
    }


    private void setupDatePickers() {
        datePickerFrom = new DatePickerFragment();
        datePickerTo = new DatePickerFragment();
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
        binding.purchaseFilterFromDate.setOnClickListener((v) -> datePickerFrom.show(getParentFragmentManager(), "datePickerFrom"));
        binding.purchaseFilterToDate.setOnClickListener((v) -> datePickerTo.show(getParentFragmentManager(), "datePickerTo"));
        binding.filterWeek.setOnClickListener((v) -> {
            LocalDate from = LocalDate.now().minusDays(7);
            quickUpdateFilter(from);
        });
        binding.filterMonth.setOnClickListener((v) -> {
            LocalDate from = LocalDate.now().withDayOfMonth(1);
            quickUpdateFilter(from);
        });
        binding.filter3month.setOnClickListener((v) -> {
            LocalDate from = LocalDate.now().minusMonths(3).withDayOfMonth(1);
            quickUpdateFilter(from);
        });
        binding.filter6month.setOnClickListener((v) -> {
            LocalDate from = LocalDate.now().minusMonths(6).withDayOfMonth(1);
            quickUpdateFilter(from);
        });
        binding.filterYear.setOnClickListener((v) -> {
            LocalDate from = LocalDate.now().withMonth(1).withDayOfMonth(1);
            quickUpdateFilter(from);
        });
        binding.filterLastYear.setOnClickListener((v) -> {
            LocalDate from = LocalDate.now().minusYears(1).withMonth(1).withDayOfMonth(1);
            LocalDate to = LocalDate.now().minusYears(1).withMonth(12).withDayOfMonth(31);
            quickUpdateFilter(from, to);
        });
    }

    private void quickUpdateFilter(LocalDate from) {
        quickUpdateFilter(from, LocalDate.now());
    }

    private void quickUpdateFilter(LocalDate from, LocalDate to) {
        boolean toHasChanged = !filter.getTo().equals(to);
        boolean fromHasChanged = !filter.getFrom().equals(from);
        datePickerFrom.setValue(from);
        filter.setFrom(from);
        datePickerTo.setValue(to);
        filter.setTo(to);
        Animation shake = AnimationUtils.loadAnimation(this.getContext(), R.anim.shake);

        if (fromHasChanged) binding.purchaseFilterFromDate.startAnimation(shake);
        if (toHasChanged) binding.purchaseFilterToDate.startAnimation(shake);
    }

    private void setupCategorySpinner(boolean containCategory) {
        if (!containCategory) {
            binding.purchaseFilterCategoryInput.setVisibility(View.GONE);
            return;
        }
        new Thread(() -> {
            categoryOptions = purchaseClient.getAllCategories();
            categoryOptions.add(0, new CategoryView(null, "None", "#fff"));
            new Handler(Looper.getMainLooper()).post(() -> {
                updateFilter(filterViewModel.getFilterValue());
            });
        }).start();
        binding.purchaseFilterCategoryInput.setItems(categoryOptions.stream().map(CategoryFilter::new).collect(Collectors.toList()),
                (options) -> filter.setCategories((List<CategoryFilter>) options));
    }

    private void fillEditForm(PurchaseFilter view) {
        new Thread(() -> {
            if (view.getFrom() != null && datePickerFrom != null) {
                datePickerFrom.getDateResult().postValue(view.getFrom());
            }
            if (view.getTo() != null && datePickerTo != null) {
                datePickerTo.getDateResult().postValue(view.getTo());
            }
            if (view.getCategories() != null && binding != null && binding.purchaseFilterCategoryInput.getAdapter() != null) {
                binding.purchaseFilterCategoryInput.setSelected(view.getCategories());
            }
        }).start();

    }

    private void resetForm() {
        this.updateFilter(getDefaultFilter());
        binding.purchaseFilterCategoryInput.setSelection(0);
        binding.purchaseFilterToDate.setText(R.string.to);
        binding.purchaseFilterFromDate.setText(R.string.from);
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

    private void updateFilter(PurchaseFilter observedFilter) {
        filter = observedFilter;
        this.fillEditForm(filter);
    }
}
