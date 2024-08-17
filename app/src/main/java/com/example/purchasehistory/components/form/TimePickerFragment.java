package com.example.purchasehistory.components.form;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.MutableLiveData;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

import java.time.LocalTime;
import java.util.Calendar;

@Getter
public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {
    private final MutableLiveData<LocalTime> timeResult;

    public TimePickerFragment(LocalTime timeResult) {
            this.timeResult = new MutableLiveData<>(timeResult != null ? timeResult : LocalTime.now());
    }

    public TimePickerFragment() {
        timeResult = new MutableLiveData<>();
    }

    @Override
    public @NotNull Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker.
        int hour;
        int minute;
        if(timeResult.getValue() != null){
            hour = timeResult.getValue().getHour();
            minute = timeResult.getValue().getMinute();
        }
        else {
            final Calendar c = Calendar.getInstance();
            hour = c.get(Calendar.HOUR_OF_DAY);
            minute = c.get(Calendar.MINUTE);
        }

        // Create a new instance of TimePickerDialog and return it.
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        this.timeResult.postValue(LocalTime.of(hourOfDay, minute));
    }

    public void setValue(LocalTime time) {
        this.timeResult.postValue(time);
    }
}