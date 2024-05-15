package com.example.purchasehistory.components.form;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.MutableLiveData;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Calendar;
@Getter
public class DatePickerFragment extends DialogFragment
        implements DatePickerDialog.OnDateSetListener {


    private MutableLiveData<LocalDate> dateResult = new MutableLiveData<>(LocalDate.now());
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current date as the default date in the picker.
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        // Create a new instance of DatePickerDialog and return it.
        return new DatePickerDialog(requireContext(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        this.dateResult.postValue(LocalDate.of(year,month,day));
    }
}