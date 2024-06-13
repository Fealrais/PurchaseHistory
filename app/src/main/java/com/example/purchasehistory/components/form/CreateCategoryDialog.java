package com.example.purchasehistory.components.form;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import androidx.fragment.app.DialogFragment;
import com.angelp.purchasehistorybackend.models.views.incoming.CategoryDTO;
import com.example.purchasehistory.R;
import com.example.purchasehistory.data.model.Category;
import com.example.purchasehistory.databinding.CategoryDialogBinding;
import com.example.purchasehistory.web.clients.PurchaseClient;
import dagger.hilt.android.AndroidEntryPoint;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

@AndroidEntryPoint
public class CreateCategoryDialog extends DialogFragment {
    private final String TAG = "CategoryDialog";
    @Inject
    PurchaseClient purchaseClient;
    private CategoryDialogBinding binding;

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
                            Bundle bundle = new Bundle();
                            bundle.putParcelable("newCategoryView", category);
                            getParentFragmentManager().setFragmentResult("categoryResult", bundle);
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
}
