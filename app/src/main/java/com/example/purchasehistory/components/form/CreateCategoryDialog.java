package com.example.purchasehistory.components.form;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import androidx.fragment.app.DialogFragment;
import com.example.purchasehistory.R;
import com.example.purchasehistory.databinding.CategoryDialogBinding;
import com.example.purchasehistory.web.clients.PurchaseClient;
import dagger.hilt.android.AndroidEntryPoint;
import org.jetbrains.annotations.NotNull;

@AndroidEntryPoint
public class CreateCategoryDialog extends DialogFragment {
    private final String TAG = "CategoryDialog";
    private CategoryDialogBinding binding;
    PurchaseClient purchaseClient;

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        binding = CategoryDialogBinding.inflate(getLayoutInflater());
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(R.layout.category_dialog)
                .setPositiveButton(R.string.create_category, (dialog, id) -> {
                    String name = binding.categoryNameInput.getText().toString();
                    String color = binding.categoryColorInput.getText().toString();
                    Log.i(TAG, "onCreateDialog: WIP");
                    dialog.dismiss();
//                    validateValues(name, color);
//                    new Thread(()-> {
//                        Category category = (Category) purchaseClient.createCategory(new CategoryDTO(name, color));
//                        savedInstanceState.putParcelable("categoryResult", category);
//                    }
//                    ).start();

                })
                .setNegativeButton(R.string.cancel, (dialog, id) -> dialog.dismiss());
        // Create the AlertDialog object and return it.
        return builder.create();
    }


    private void validateValues(String name, String color) {

    }
}
