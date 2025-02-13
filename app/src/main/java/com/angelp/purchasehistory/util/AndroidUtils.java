package com.angelp.purchasehistory.util;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.format.DateUtils;
import android.util.Log;
import android.util.Patterns;
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
import com.github.mikephil.charting.charts.Chart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
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
        try {
            if (category != null && category.getColor() != null && !category.getColor().isBlank())
                return Color.parseColor(category.getColor());
            else return Color.GRAY;
        } catch (IllegalArgumentException e) {
            Log.e("AndroidUtils", "Invalid color: " + category.getColor());
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

    public static void openCsvFile(Context context, Uri uri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "text/csv");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        // Check if there's an activity available to handle this intent
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        } else {
            // Handle the case when there's no activity available to handle the intent
            PurchaseHistoryApplication.getInstance().alert("No application available to open CSV files");
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
    public static void initChart(Chart<?> chart, AppColorCollection colors, String format){
//        chart.setBackgroundColor(colors.getBackgroundColor());
        chart.getXAxis().setTextColor(colors.getForegroundColor());
        chart.getLegend().setTextColor(colors.getForegroundColor());
        // Grid and Borders
        chart.getXAxis().setGridColor(colors.getMiddleColor());
        chart.getLegend().setTextColor(colors.getForegroundColor());
        chart.getDescription().setTextColor(colors.getForegroundColor());

        chart.setExtraOffsets(5, 10, 5, 5);
        chart.setDragDecelerationFrictionCoef(0.95f);
        chart.setHighlightPerTapEnabled(true);
        chart.getDescription().setEnabled(false);
        chart.setMinimumHeight(Constants.GRAPH_MIN_HEIGHT);
        DayAxisValueFormatter xAxisFormatter = new DayAxisValueFormatter(format);

        XAxis xLabels = chart.getXAxis();
        xLabels.setPosition(XAxis.XAxisPosition.BOTTOM);
        xLabels.setValueFormatter(xAxisFormatter);
        xLabels.setGranularity(1f);
        xLabels.setTextColor(colors.getForegroundColor());

        Legend l = chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setTextColor(colors.getForegroundColor());
        l.setDrawInside(true);
        l.setFormSize(8f);
        l.setFormToTextSpace(4f);
        l.setXEntrySpace(6f);

    }
}
