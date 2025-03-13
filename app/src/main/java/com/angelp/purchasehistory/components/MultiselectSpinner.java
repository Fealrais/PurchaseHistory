package com.angelp.purchasehistory.components;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import com.angelp.purchasehistory.data.filters.MultiSelectOption;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MultiselectSpinner extends androidx.appcompat.widget.AppCompatSpinner implements
        DialogInterface.OnMultiChoiceClickListener, DialogInterface.OnCancelListener {

        private List<MultiSelectOption> items;
        private MultiselectSpinnerListener listener;

        public MultiselectSpinner(Context context) {
            super(context);
        }

        public MultiselectSpinner(Context arg0, AttributeSet arg1) {
            super(arg0, arg1);
        }

        public MultiselectSpinner(Context arg0, AttributeSet arg1, int arg2) {
            super(arg0, arg1, arg2);
        }

        @Override
        public void onClick(DialogInterface dialog, int which, boolean isChecked) {
            MultiSelectOption item = (MultiSelectOption) getItemAtPosition(which);
            item.setSelected(isChecked);
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            // refresh text on spinner
            StringBuilder spinnerBuffer = new StringBuilder();
            List<MultiSelectOption> selectedItems = new ArrayList<>();
            for (MultiSelectOption multiSelectOption : items) {
                if (multiSelectOption.isSelected()) {
                    spinnerBuffer.append(multiSelectOption);
                    spinnerBuffer.append(", ");
                    selectedItems.add(multiSelectOption);
                }
            }
            String spinnerText = spinnerBuffer.toString();
            if (spinnerText.length() > 2)
                spinnerText = spinnerText.substring(0, spinnerText.length() - 2);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                    android.R.layout.simple_spinner_item,
                    new String[] { spinnerText });
            setAdapter(adapter);
            listener.onItemsSelected(selectedItems);
        }

        @Override
        public boolean performClick() {
            super.performClick();
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            boolean[] selected = new boolean[items.size()];
            String[] labels = new String[items.size()];
            for (int i = 0; i < items.size(); i++) {
                selected[i] = items.get(i).isSelected();
                labels[i] = items.get(i).getLabel();
            }
            builder.setMultiChoiceItems(labels, selected, this);
            builder.setPositiveButton(android.R.string.ok,
                    (dialog, which) -> dialog.cancel());
            builder.setOnCancelListener(this);
            builder.show();
            return true;
        }

        public void setSelected(List<? extends MultiSelectOption> selectedItems) {
            StringBuilder spinnerBuffer = new StringBuilder();
            items = items.stream().peek((item)-> {
                item.setSelected(selectedItems.contains(item));
                if(item.isSelected()) {
                    spinnerBuffer.append(item);
                    spinnerBuffer.append(", ");
                }
            }).collect(Collectors.toList());
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                    android.R.layout.simple_spinner_item, new String[] { spinnerBuffer.toString() });
            new Handler(Looper.getMainLooper()).post(() -> setAdapter(adapter));
        }
        public void setItems(List<MultiSelectOption> items,
                             MultiselectSpinnerListener listener) {
            this.items = items;
            this.listener = listener;
            StringBuilder spinnerBuffer = new StringBuilder();
            for (MultiSelectOption multiSelectOption : items) {
                if (multiSelectOption.isSelected()) {
                    spinnerBuffer.append(multiSelectOption);
                    spinnerBuffer.append(", ");
                }
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                    android.R.layout.simple_spinner_item, new String[] { spinnerBuffer.toString() });
            setAdapter(adapter);
        }

        public interface MultiselectSpinnerListener {
            void onItemsSelected(List<? extends MultiSelectOption> selected);
        }
    }
