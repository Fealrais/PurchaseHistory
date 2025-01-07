package com.angelp.purchasehistory.ui.home.graph;

import com.github.mikephil.charting.formatter.ValueFormatter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DayAxisValueFormatter extends ValueFormatter {
    @Override
    public String getFormattedValue(float value) {
        LocalDate localDate = LocalDate.ofEpochDay(((Float) value).longValue());
        return localDate.format(DateTimeFormatter.ofPattern("dd MMM"));
    }
}
