package com.angelp.purchasehistory.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.Looper;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import com.angelp.purchasehistory.MainActivity;
import com.angelp.purchasehistory.PurchaseHistoryApplication;
import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.data.AppColorCollection;
import com.angelp.purchasehistory.data.Constants;
import com.angelp.purchasehistory.ui.home.dashboard.graph.DayAxisValueFormatter;
import com.angelp.purchasehistorybackend.models.enums.ScheduledPeriod;
import com.angelp.purchasehistorybackend.models.views.outgoing.CategoryView;
import com.angelp.purchasehistorybackend.models.views.outgoing.ScheduledExpenseView;
import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public final class AndroidUtils {
    public static final List<String> SCHEDULED_PERIOD_LIST = Arrays.stream(ScheduledPeriod.values()).map(Enum::toString).collect(Collectors.toList());
    public static final int SAVE_CSV_REQUEST_CODE = 199999;

    public static void shareString(String token, String title, Context context) {
        Log.i("Sharing", "Attempting to share a string.");
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, token);
        sendIntent.putExtra(Intent.EXTRA_TITLE, title);

        sendIntent.setType("text/plain");

        Intent shareIntent = Intent.createChooser(sendIntent, null);
        context.startActivity(shareIntent);
    }

    public static void logout(Context context) {
        PurchaseHistoryApplication.getInstance().userToken.postValue(null);
        PurchaseHistoryApplication.getInstance().loggedUser.postValue(null);
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    public static int getColor(CategoryView category) {
        if (category == null || category.getColor() == null || category.getColor().isBlank())
            return Color.GRAY;
        String colorHex = category.getColor();
        return getColor(colorHex);
    }

    public static int getColor(String colorHex) {
        try {
            return Color.parseColor(colorHex);
        } catch (IllegalArgumentException e) {
            Log.e("AndroidUtils", "Invalid color: " + colorHex);
            return Color.GRAY;
        }
    }

    public static int getTextColor(int bgColor) {
        if (Color.luminance(bgColor) > 0.5)
            return Color.BLACK;
        else
            return Color.WHITE;
    }

    // A placeholder username validation check
    public static boolean isUserNameValid(String username) {
        if (username == null || username.trim().length() <= 5) {
            return false;
        } else {
            return !username.trim().isEmpty();
        }
    }

    // A placeholder email validation check
    public static boolean isEmailValid(String email) {
        if (email == null) {
            return false;
        } else return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }


    // A placeholder password validation check
    public static boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }

    public static void openCsvFile(Activity context, String name) {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.setType("text/csv");
        intent.putExtra(Intent.EXTRA_TITLE, name);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Check if there's an activity available to handle this intent
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivityForResult(intent, SAVE_CSV_REQUEST_CODE);
        } else {
            // Handle the case when there's no activity available to handle the intent
            PurchaseHistoryApplication.getInstance().alert("No application available to save CSV files");
        }
    }

    @NotNull
    public static String getNextTimestampString(ScheduledExpenseView scheduledExpense) {
        if (scheduledExpense == null || scheduledExpense.getPeriod() == null) {
            return "";
        }

        LocalDateTime nextTimestamp = scheduledExpense.getPeriod().getNextTimestamp(scheduledExpense.getTimestamp());

        return (String) DateUtils.getRelativeTimeSpanString(nextTimestamp.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
    }

    public static void setNextTimestampString(TextView textView, ScheduledExpenseView scheduledExpense) {
        if (scheduledExpense == null || scheduledExpense.getPeriod() == null) {
            textView.setText(R.string.no_period_selected);
            return;
        }
        if (!scheduledExpense.isEnabled()) {
            textView.setText(R.string.disabled);
            return;
        }
        String nextTimestampString = getNextTimestampString(scheduledExpense);
        if (nextTimestampString.isBlank()) {
            textView.setText("");
            return;
        }
        textView.setText(getNextTimestampString(scheduledExpense));
    }

    @NotNull
    public static String formatCurrency(BigDecimal price) {
        return String.format(Locale.getDefault(), "%.2f", price.floatValue());
    }
    @NotNull
    public static String formatCurrency(BigDecimal price, Context context) {
        return formatCurrency(price.floatValue(), context);
    }
    @NotNull
    public static String formatCurrency(float price, Context context) {
        return String.format(Locale.getDefault(), "%.2f %s", price, getCurrencySymbol(context));
    }

    public static String getCurrencySymbol(Context context) {
        SharedPreferences appPreferences = context.getSharedPreferences(Constants.Preferences.APP_PREFERENCES, Context.MODE_PRIVATE);
        return appPreferences.getString(Constants.Preferences.PREFERRED_CURRENCY, "");
    }

    public static void initChart(BarLineChartBase<?> chart, AppColorCollection colors, String format, Typeface tf) {
//        chart.setBackgroundColor(colors.getBackgroundColor());
        chart.getXAxis().setTextColor(colors.getForegroundColor());
        chart.getLegend().setTextColor(colors.getForegroundColor());
        // Grid and Borders
        chart.getXAxis().setGridColor(colors.getMiddleColor());
        chart.getLegend().setTextColor(colors.getForegroundColor());
        chart.getDescription().setTextColor(colors.getForegroundColor());
        chart.getDescription().setTypeface(tf);
        chart.setExtraOffsets(5, 10, 5, 5);
        chart.setDragDecelerationFrictionCoef(0.95f);
        chart.setHighlightPerTapEnabled(true);
        chart.setHighlightPerDragEnabled(false);
        chart.getDescription().setEnabled(false);
        chart.setMinimumHeight(Constants.GRAPH_MIN_HEIGHT);
        DayAxisValueFormatter xAxisFormatter = new DayAxisValueFormatter(format);
        // change the position of the y-labels
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)
        leftAxis.setTextColor(colors.getForegroundColor());
        leftAxis.setTypeface(tf);
        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);

        XAxis xLabels = chart.getXAxis();
        xLabels.setPosition(XAxis.XAxisPosition.BOTTOM);
        xLabels.setValueFormatter(xAxisFormatter);
        xLabels.setGranularity(1f);
        xLabels.setTextColor(colors.getForegroundColor());
        xLabels.setTypeface(tf);
        Legend l = chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setTextColor(colors.getForegroundColor());
        l.setDrawInside(true);
        l.setFormSize(8f);
        l.setFormToTextSpace(4f);
        l.setTypeface(tf);
        l.setXEntrySpace(6f);

    }

    public static void showSuccessAnimation(View view) {
        if (view == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
        LayoutInflater inflater = LayoutInflater.from(view.getContext());
        View customView = inflater.inflate(R.layout.success_toast, null);
        builder.setView(customView);

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            AlertDialog dialog = builder.create();
            if (dialog.getWindow() != null)
                dialog.getWindow().getAttributes().windowAnimations = R.anim.success_toast;
            dialog.show();
            handler.postDelayed(dialog::dismiss, 1000);
        });

    }

    public static boolean validateCategoryValues(AutoCompleteTextView nameView, AutoCompleteTextView colorView) {
        Context context = nameView.getContext();
        String name = nameView.getText().toString();
        String color = colorView.getText().toString();
        if (name.trim().isEmpty()) {
            nameView.setError(context.getString(R.string.error_name_empty));
            return false;
        } else nameView.setError(null);
        if (color.trim().isEmpty()) {
            colorView.setError(context.getString(R.string.error_color_empty));
            return false;
        } else if (!color.startsWith("#")) {
            colorView.setError(context.getString(R.string.error_color_invalid));
            return false;
        } else if (color.length() < 6) {
            colorView.setError(context.getString(R.string.error_color_length));
            return false;
        } else colorView.setError(null);
        return true;
    }

    public static void validateNumber(String string) {
        try {
            BigDecimal bigDecimal = new BigDecimal(string);
            if (bigDecimal.floatValue() <= 0) {
                throw new IllegalArgumentException("Invalid number: " + string);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number: " + string);
        }
    }
}
