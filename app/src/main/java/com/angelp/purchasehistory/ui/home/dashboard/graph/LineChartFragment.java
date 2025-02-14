package com.angelp.purchasehistory.ui.home.dashboard.graph;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.data.AppColorCollection;
import com.angelp.purchasehistory.data.Constants;
import com.angelp.purchasehistory.data.filters.PurchaseFilter;
import com.angelp.purchasehistory.data.interfaces.RefreshablePurchaseFragment;
import com.angelp.purchasehistory.databinding.FragmentLineChartBinding;
import com.angelp.purchasehistory.ui.home.dashboard.purchases.PurchaseFilterDialog;
import com.angelp.purchasehistory.util.AndroidUtils;
import com.angelp.purchasehistory.web.clients.PurchaseClient;
import com.angelp.purchasehistorybackend.models.views.outgoing.analytics.CalendarReport;
import com.angelp.purchasehistorybackend.models.views.outgoing.analytics.CalendarReportEntry;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import dagger.hilt.android.AndroidEntryPoint;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AndroidEntryPoint
@NoArgsConstructor
public class LineChartFragment extends RefreshablePurchaseFragment implements OnChartValueSelectedListener {
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM yy");
    private final String TAG = this.getClass().getSimpleName();
    private final PurchaseFilterDialog filterDialog = new PurchaseFilterDialog(true);
    @Inject
    PurchaseClient purchaseClient;
    AlertDialog.Builder alertBuilder;
    private FragmentLineChartBinding binding;
    private boolean showFilter;
    private AppColorCollection appColorCollection;
    private AlertDialog dialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            showFilter = getArguments().getBoolean(Constants.ARG_SHOW_FILTER);
        }
        alertBuilder = new AlertDialog.Builder(getActivity());
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: View created");
        binding = FragmentLineChartBinding.inflate(inflater, container, false);
        appColorCollection = new AppColorCollection(inflater.getContext());
        return binding.getRoot();
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.applyFilter(filterViewModel.getFilterValue());
        initFilterRow();
        initGraph(binding.lineChartView);
        setData(filterViewModel.getFilterValue());
    }

    private void initFilterRow() {
        binding.graphFilterButton.setOnClickListener((v) -> openFilter());
        binding.textView.setTextColor(getContext().getColor(R.color.foreground_color));
        new Handler(Looper.getMainLooper()).post(() -> {
            binding.graphFilterButton.setVisibility(showFilter ? View.VISIBLE : View.GONE);
            binding.textView.setVisibility(showFilter ? View.VISIBLE : View.GONE);
        });
    }

    private void initGraph(LineChart chart) {
        AndroidUtils.initChart(chart, appColorCollection, "dd");
        if (showFilter) {
            chart.setOnChartValueSelectedListener(this);
        }
        chart.setMinimumHeight(Constants.GRAPH_MIN_HEIGHT);
        // change the position of the y-labels
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTextColor(appColorCollection.getForegroundColor());
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)
        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);
    }

    private void setData(PurchaseFilter filter) {
        new Thread(() -> {
            CalendarReport calendarReport = purchaseClient.getCalendarReport(filter);
            Map<LocalDate, List<Entry>> entriesMap = getEntries(calendarReport);
            ArrayList<Integer> colors = getColors();
            LineData data = new LineData();
            int i = 0;
            for (Map.Entry<LocalDate, List<Entry>> entry : entriesMap.entrySet()) {
                List<Entry> entries = entry.getValue();

                LineDataSet lineDataSet = new LineDataSet(entries, "Purchases");
//                lineDataSet.setDrawIcons(false);
                lineDataSet.setDrawCircleHole(false);
                if (i < colors.size()) {
                    int color = colors.get(i++);
                    lineDataSet.setColor(color);
                    lineDataSet.setCircleColor(color);
                }
                lineDataSet.setLabel(entry.getKey().format(DATE_TIME_FORMATTER));
                data.addDataSet(lineDataSet);
            }
            data.setValueTextColor(appColorCollection.getForegroundColor());
            notifyDataChanged(data);
        }).start();
    }

    private ArrayList<Integer> getColors() {
        ArrayList<Integer> list = new ArrayList<>();
        Context context = getContext();
        Resources resources = getResources();
        if (context == null) return list;
        list.add(resources.getColor(R.color.line_chart_color_1, context.getTheme()));
        list.add(resources.getColor(R.color.line_chart_color_2, context.getTheme()));
        list.add(resources.getColor(R.color.line_chart_color_3, context.getTheme()));
        list.add(resources.getColor(R.color.line_chart_color_4, context.getTheme()));
        list.add(resources.getColor(R.color.line_chart_color_5, context.getTheme()));
        list.add(resources.getColor(R.color.line_chart_color_6, context.getTheme()));
        list.add(resources.getColor(R.color.line_chart_color_7, context.getTheme()));
        list.add(resources.getColor(R.color.line_chart_color_8, context.getTheme()));
        list.add(resources.getColor(R.color.line_chart_color_9, context.getTheme()));
        list.add(resources.getColor(R.color.line_chart_color_10, context.getTheme()));
        list.add(resources.getColor(R.color.line_chart_color_11, context.getTheme()));
        list.add(resources.getColor(R.color.line_chart_color_12, context.getTheme()));
        return list;
    }

    @NotNull
    private Map<LocalDate, List<Entry>> getEntries(CalendarReport calendarReport) {
        Map<LocalDate, List<Entry>> map = new HashMap<>();
        for (CalendarReportEntry calendarReportEntry : calendarReport.getContent()) {
            LocalDate key = calendarReportEntry.getLocalDate().withDayOfMonth(1);
            List<Entry> entries = map.computeIfAbsent(key, (k) -> new ArrayList<>());
            Entry entry = parseEntries(calendarReportEntry);
            entries.add(entry);
            map.put(key, entries);
        }
        return map;
    }

    private void notifyDataChanged(LineData data) {
        new Handler(Looper.getMainLooper()).post(() -> {
            binding.lineChartView.setData(data);
            binding.lineChartView.getData().notifyDataChanged();
            binding.lineChartView.notifyDataSetChanged();
            binding.lineChartView.animateY(1000);
            binding.lineChartView.invalidate();
        });
    }

    private Entry parseEntries(CalendarReportEntry entry) {
        float x = ((Long) entry.getLocalDate()
                .withYear(LocalDate.now().getYear())
                .with(Month.JANUARY)
                .toEpochDay()).floatValue();

        return new Entry(x, entry.getSum().floatValue(), entry);
    }

    private void openFilter() {
        filterDialog.show(getParentFragmentManager(), "purchasesFilterDialog");
    }



    private void applyFilter(PurchaseFilter newFilter) {
        binding.graphFilterButton.setText(R.string.filterButton);
        binding.textView.setText(newFilter.getReadableString());
    }

    public void refresh(PurchaseFilter filter) {
        new Thread(() -> {
            setData(filter);
            getActivity().runOnUiThread(() -> {
                binding.lineChartView.notifyDataSetChanged();
                binding.lineChartView.animateY(1400, Easing.EaseInOutQuad);
            });
        }).start();
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        if (e == null)
            return;
        CalendarReportEntry data = (CalendarReportEntry) e.getData();
        setTooltipText(data.getLocalDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")), getDescription(data));
    }

    @Override
    public void onNothingSelected() {

    }

    public String getDescription(CalendarReportEntry entry) {
        return getString(R.string.total_sum) + ": " + entry.getSum() + "\n" +
                getString(R.string.number_of_purchases) + ": " + entry.getCount();
    }

    private void setTooltipText(String title, String text) {
        if (dialog != null && dialog.isShowing()) dialog.dismiss();

        alertBuilder.setMessage(text).setTitle(title);
        dialog = alertBuilder.create();
        dialog.show();
    }
}