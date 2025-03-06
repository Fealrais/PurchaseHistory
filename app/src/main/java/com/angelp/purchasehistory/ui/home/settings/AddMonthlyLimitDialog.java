package com.angelp.purchasehistory.ui.home.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import androidx.fragment.app.DialogFragment;
import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.util.AndroidUtils;
import com.angelp.purchasehistory.web.clients.SettingsClient;
import com.angelp.purchasehistorybackend.models.views.incoming.MonthlyLimitDTO;
import com.angelp.purchasehistorybackend.models.views.outgoing.MonthlyLimitView;
import dagger.hilt.android.AndroidEntryPoint;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.function.Consumer;

@AndroidEntryPoint
public class AddMonthlyLimitDialog extends DialogFragment {

    private final Consumer<MonthlyLimitView> monthlyLimitViewConsumer;

    public AddMonthlyLimitDialog(Consumer<MonthlyLimitView> consumer) {
        monthlyLimitViewConsumer = consumer;
    }

    @Inject
    SettingsClient settingsClient;

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_edit_monthly_limit, null);
        EditText limitValue = view.findViewById(R.id.editTextMonthlyLimit);
        EditText limitLabel = view.findViewById(R.id.editTextMonthlyLimitLabel);
        builder.setView(view);
        builder.setTitle(R.string.add_monthly_limit);
        Button buttonSave = view.findViewById(R.id.buttonSave);
        Button buttonCancel = view.findViewById(R.id.buttonCancel);
        buttonSave.setOnClickListener((v) -> {
            try {
                AndroidUtils.validateNumber(limitValue.getText().toString());

                MonthlyLimitDTO monthlyLimitDTO = new MonthlyLimitDTO();
                monthlyLimitDTO.setValue(new BigDecimal(limitValue.getText().toString()));
                monthlyLimitDTO.setLabel(limitLabel.getText().toString());
                new Thread(() -> {
                    MonthlyLimitView result = settingsClient.addMonthlyLimit(monthlyLimitDTO);
                    monthlyLimitViewConsumer.accept(result);
                    dismiss();
                }).start();

            } catch (IllegalArgumentException e) {
                limitValue.setError(getString(R.string.invalid_number));
            }
        });
        buttonCancel.setOnClickListener((v) -> {
            Dialog dialog = getDialog();
            if (dialog != null) dialog.cancel();
        });
        return builder.create();
    }
}
