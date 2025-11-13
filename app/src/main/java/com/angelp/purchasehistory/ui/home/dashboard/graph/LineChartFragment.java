package com.angelp.purchasehistory.ui.home.dashboard.graph;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
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
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import dagger.hilt.android.AndroidEntryPoint;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AndroidEntryPoint
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
    private Typeface tf;
    private PurchasesPerDayDialog dialog;

    public LineChartFragment() {
        Bundle args = new Bundle();
        this.setArguments(args);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            showFilter = getArguments().getBoolean(Constants.Arguments.ARG_SHOW_FILTER);
        }
        alertBuilder = new AlertDialog.Builder(getActivity());
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: View created");
        binding = FragmentLineChartBinding.inflate(inflater, container, false);
        appColorCollection = new AppColorCollection(inflater.getContext());
        tf = ResourcesCompat.getFont(inflater.getContext(), R.font.ibmplexmono_regular);
        super.setLoadingScreen(binding.loadingBar);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (binding == null) return;
        this.applyFilter(filterViewModel.getFilterValue());
        initFilterRow();
        initGraph(binding.lineChartView);
        setData(filterViewModel.getFilterValue());
    }

    private void initFilterRow() {
        binding.graphFilterButton.setOnClickListener((v) -> openFilter());
        binding.textView.setTextColor(getContext().getColor(R.color.text));
        new Handler(Looper.getMainLooper()).post(() -> {
            binding.graphFilterButton.setVisibility(showFilter ? View.VISIBLE : View.GONE);
            binding.textView.setVisibility(showFilter ? View.VISIBLE : View.GONE);
        });
    }

    private void initGraph(LineChart chart) {
        AndroidUtils.initChart(chart, appColorCollection, "dd", tf);
        chart.setOnChartValueSelectedListener(this);
        chart.setNoDataText(getString(R.string.no_data));

    }

    private void setData(PurchaseFilter filter) {
        new Thread(() -> {
            isRefreshing.postValue(true);
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
                lineDataSet.setValueTypeface(tf);
                lineDataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
                if (i < colors.size()) {
                    int color = colors.get(i++);
                    lineDataSet.setColor(color);
                    lineDataSet.setCircleColor(color);
                }
                lineDataSet.setLabel(entry.getKey().format(DATE_TIME_FORMATTER));
                data.addDataSet(lineDataSet);
            }
            data.setValueTextColor(appColorCollection.getForegroundColor());
            data.setValueFormatter(new CurrencyValueFormatter(AndroidUtils.getCurrencySymbol(getContext())));
            notifyDataChanged(data);
            isRefreshing.postValue(false);
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
        if (data == null || data.getDataSetCount() == 0) return;

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
        binding.textView.setText(newFilter.getDateString());
    }

    public void refresh(PurchaseFilter filter) {
        if (binding == null) return;
        new Thread(() -> setData(filter)).start();
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        if (e == null)
            return;
        if (dialog != null && dialog.isAdded()) dialog.dismiss();

        CalendarReportEntry data = (CalendarReportEntry) e.getData();
        if (!data.getSum().equals(BigDecimal.ZERO)) {
            dialog = new PurchasesPerDayDialog(data);
            dialog.show(getParentFragmentManager().beginTransaction(), "DialogFragment");
        }

    }

    @Override
    public void onNothingSelected() {

    }
}