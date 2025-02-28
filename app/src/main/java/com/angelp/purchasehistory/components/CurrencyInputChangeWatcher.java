package com.angelp.purchasehistory.components;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.lang.ref.WeakReference;
import java.util.Locale;

public abstract class CurrencyInputChangeWatcher implements TextWatcher {
    private String current = "";
    private final WeakReference<EditText> editTextWeakReference;

    public CurrencyInputChangeWatcher(EditText editTextWeakReference) {
        this.editTextWeakReference = new WeakReference<>(editTextWeakReference);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (!s.toString().equals(current) && editTextWeakReference.get() != null) {
            EditText editText = editTextWeakReference.get();
            editText.removeTextChangedListener(this);

            String cleanString = s.toString().replaceAll("[,.]", "");

            double parsed = Double.parseDouble(cleanString);
            String formatted = String.format(Locale.getDefault(), "%.2f", parsed / 100);

            current = formatted;
            editText.setText(formatted);
            editText.setSelection(formatted.length());

            editText.addTextChangedListener(this);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {
    }
}
