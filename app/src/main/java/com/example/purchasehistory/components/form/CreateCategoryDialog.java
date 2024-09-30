package com.example.purchasehistory.components.form;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import com.angelp.purchasehistorybackend.models.views.incoming.CategoryDTO;
import com.angelp.purchasehistorybackend.models.views.outgoing.CategoryView;
import com.example.purchasehistory.R;
import com.example.purchasehistory.data.model.Category;
import com.example.purchasehistory.databinding.CategoryDialogBinding;
import com.example.purchasehistory.web.clients.PurchaseClient;
import dagger.hilt.android.AndroidEntryPoint;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.util.function.Consumer;

@AndroidEntryPoint
public class CreateCategoryDialog extends DialogFragment {
    private final String TAG = this.getClass().getSimpleName();
    private Consumer<CategoryView> consumer;
    @Inject
    PurchaseClient purchaseClient;
    private CategoryDialogBinding binding;


    public CreateCategoryDialog() {
    }

    public CreateCategoryDialog(Consumer<CategoryView> categoryViewProvider) {
        this.consumer = categoryViewProvider;
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        binding = CategoryDialogBinding.inflate(getLayoutInflater());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(binding.getRoot())
                .setPositiveButton(R.string.create_category, (dialog, id) -> {
                    String name = binding.categoryNameInput.getText().toString();
                    String color = binding.categoryColorInput.getText().toString();
                    try {
                        validateValues(name, color);
                        binding.categoryErrorText.setError("");
                        new Thread(() -> {
                            Category category = purchaseClient.createCategory(new CategoryDTO(name, color));
                            consumer.accept(category);
                            dialog.dismiss();
                        }).start();
                    } catch (RuntimeException e) {
                        binding.categoryErrorText.setText(e.getMessage());
                        Log.i(TAG, "onCreateDialog: Validation failed: " + e.getMessage());
                    }
                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss());
        return builder.create();
    }


    private void validateValues(String name, String color) {
        if (name.trim().isEmpty())
            throw new RuntimeException("Name cannot be empty");
        if (color.trim().isEmpty())
            throw new RuntimeException("Color cannot be empty");
        if (!color.startsWith("#"))
            throw new RuntimeException("Color should be a hex value");
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
