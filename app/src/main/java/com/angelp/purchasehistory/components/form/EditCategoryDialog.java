package com.angelp.purchasehistory.components.form;

import android.app.AlertDialog;
import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.util.Log;
import android.widget.ArrayAdapter;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import com.angelp.purchasehistory.data.model.Category;
import com.angelp.purchasehistory.databinding.CategoryDialogBinding;
import com.angelp.purchasehistory.util.AfterTextChangedWatcher;
import com.angelp.purchasehistory.util.AndroidUtils;
import com.angelp.purchasehistory.web.clients.PurchaseClient;
import com.angelp.purchasehistorybackend.models.views.incoming.CategoryDTO;
import com.angelp.purchasehistorybackend.models.views.outgoing.CategoryView;
import dagger.hilt.android.AndroidEntryPoint;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@AndroidEntryPoint
public class EditCategoryDialog extends DialogFragment {
    private final String TAG = this.getClass().getSimpleName();
    @Inject
    PurchaseClient purchaseClient;
    List<String> defaultCategories = List.of("Groceries", "Medicine", "Travel", "Gifts", "Fast food", "Hobbies", "Bills", "Investments", "Essentials");
    List<String> defaultColors = List.of("#FADADD", "#AECBFA", "#D4EED1", "#FFF9C4", "#E6E6FA", "#FFDAB9", "#F5FFFA", "#F08080");
    private CategoryView defaultValue;
    private Long purchaseId;
    private Consumer<CategoryView> consumer;

    private CategoryDialogBinding binding;

    public EditCategoryDialog() {
    }

    public EditCategoryDialog(Long id, CategoryView defaultValue, Consumer<CategoryView> categoryViewProvider) {
        this.purchaseId = id;
        this.defaultValue = defaultValue;
        this.consumer = categoryViewProvider;
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        binding = CategoryDialogBinding.inflate(getLayoutInflater());
        fillNameAutocomplete();
        binding.colorPickerView.setHueSliderView(binding.hueSlider);
        binding.colorPickerView.setOnColorChangedListener((color) -> {
            if (binding.colorPickerView.hasWindowFocus()) {
                binding.categoryColorInput.setText(String.format("#%06X", (0xFFFFFF & color)));
            }
        });
        binding.categoryColorInput.addTextChangedListener(new AfterTextChangedWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                String color = s.toString();
                if (color.trim().isEmpty() || !color.startsWith("#") || color.trim().length() != 7) {
                    binding.categoryColorInput.setBackgroundColor(Color.WHITE);
                    binding.categoryColorInput.setTextColor(Color.BLACK);
                    return;
                }
                int colorValue = Color.parseColor(color);
                binding.categoryColorInput.setBackgroundColor(colorValue);
                binding.categoryColorInput.setTextColor(AndroidUtils.getTextColor(colorValue));
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Editing category#" + purchaseId);
        builder.setView(binding.getRoot());
        binding.saveButton.setOnClickListener(v -> {
            String name = binding.categoryNameInput.getText().toString();
            String color = binding.categoryColorInput.getText().toString();
            boolean isValid = AndroidUtils.validateCategoryValues(binding.categoryNameInput, binding.categoryColorInput);
            if (isValid) {
                new Thread(() -> {
                    Category category = purchaseClient.editCategory(purchaseId.intValue(), new CategoryDTO(name, color));
                    defaultValue.setColor(category.getColor());
                    defaultValue.setName(category.getName());
                    consumer.accept(category);
                    dismiss();
                }).start();
            }
        });
        binding.cancelButton.setOnClickListener(v -> dismiss());
        updateUI(defaultValue);
        return builder.create();
    }

    private void updateUI(CategoryView defaultValue) {
        new Handler(Looper.getMainLooper()).post(() -> {
            binding.categoryColorInput.setText(defaultValue.getColor());
            binding.categoryNameInput.setText(defaultValue.getName());
            // FIX: Set the initial color of the color picker view
            String color = defaultValue.getColor();
            if (color != null && color.length() == 7 && color.startsWith("#")) {
                try {
                    int colorValue = Color.parseColor(color);
                    binding.colorPickerView.setColor(colorValue);
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Invalid default color for picker: " + color);
                }
            }
        });
    }

    private void fillNameAutocomplete() {
        new Thread(() -> {
            List<CategoryView> allCategories = purchaseClient.getAllCategories();

            List<String> categoryList = new ArrayList<>();
            List<String> colorList = new ArrayList<>();
            for (String defaultCategory : defaultCategories) {
                if (allCategories.stream().noneMatch(a -> defaultCategory.equals(a.getName()))) {
                    categoryList.add(defaultCategory);
                }
            }
            for (String defaultColor : defaultColors) {
                if (allCategories.stream().noneMatch(a -> defaultColor.equals(a.getColor()))) {
                    colorList.add(defaultColor);
                }
            }
            ArrayAdapter<String> nameAdapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_list_item_1, categoryList);
            new Handler(Looper.getMainLooper()).post(() -> binding.categoryNameInput.setAdapter(nameAdapter));
            ArrayAdapter<String> colorAdapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_list_item_1, colorList);
            new Handler(Looper.getMainLooper()).post(() -> binding.categoryColorInput.setAdapter(colorAdapter));
        }).start();
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
