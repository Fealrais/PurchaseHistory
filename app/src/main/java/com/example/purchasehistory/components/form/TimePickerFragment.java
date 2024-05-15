package com.example.purchasehistory.components.form;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.MutableLiveData;
import lombok.Getter;

import java.time.LocalTime;
import java.util.Calendar;

@Getter
public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
    private MutableLiveData<LocalTime> timeResult = new MutableLiveData<>(LocalTime.now());

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker.
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it.
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        this.timeResult.postValue(LocalTime.of(hourOfDay,minute));
    }
}