package com.angelp.purchasehistory.ui.home.dashboard.graph;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.data.AppColorCollection;
import com.angelp.purchasehistory.data.Constants;
import com.angelp.purchasehistory.data.filters.PurchaseFilter;
import com.angelp.purchasehistory.databinding.FragmentLineChartBinding;
import com.angelp.purchasehistory.ui.home.dashboard.RefreshableFragment;
import com.angelp.purchasehistory.ui.home.dashboard.purchases.PurchaseFilterDialog;
import com.angelp.purchasehistory.util.AndroidUtils;
import com.angelp.purchasehistory.web.clients.PurchaseClient;
import com.angelp.purchasehistorybackend.models.views.outgoing.CategoryView;
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
import java.util.function.Consumer;

import static com.angelp.purchasehistory.data.Constants.getDefaultFilter;

@AndroidEntryPoint
public class LineChartFragment extends RefreshableFragment implements OnChartValueSelectedListener {
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MMM yy");
    private final String TAG = this.getClass().getSimpleName();
    private final PurchaseFilterDialog filterDialog = new PurchaseFilterDialog(true);
    @Inject
    PurchaseClient purchaseClient;
    AlertDialog.Builder alertBuilder;
    private FragmentLineChartBinding binding;
    private boolean showFilter;
    private AppColorCollection appColorCollection;


    public LineChartFragment(PurchaseFilter filter, Consumer<PurchaseFilter> setFilter) {
        super(filter, setFilter);
        Bundle args = new Bundle();
        args.putParcelable(Constants.ARG_FILTER, filter);
        this.setArguments(args);
    }

    private static Map<LocalDate, CalendarReportEntry> prepareContent(PurchaseFilter filter, CalendarReport calendarReport, List<CategoryView> categories) {
        Map<LocalDate, CalendarReportEntry> content = new HashMap<>();

        for (CalendarReportEntry calendarReportEntry : calendarReport.getContent()) {
            content.put(calendarReportEntry.getLocalDate(), calendarReportEntry);
        }
        LocalDate dateIterator = filter.getFrom();
        for (; dateIterator.isBefore(filter.getTo().plusDays(1)); dateIterator = dateIterator.plusDays(1L)) {
            content.putIfAbsent(dateIterator, new CalendarReportEntry(dateIterator.format(DateTimeFormatter.ISO_LOCAL_DATE), BigDecimal.ZERO, 0L, null));
        }
        return content;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            filter = getArguments().getParcelable(Constants.ARG_FILTER);
            showFilter = getArguments().getBoolean(Constants.SHOW_FILTER);
        } else {
            filter = new PurchaseFilter();
            filter.setFrom(LocalDate.now().withDayOfMonth(1));
            filter.setTo(LocalDate.now());
        }
        alertBuilder = new AlertDialog.Builder(getActivity());
    }

    @Override
    public void onSaveInstanceState(@NonNull @NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(Constants.ARG_FILTER, filter);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: View created");

        binding = FragmentLineChartBinding.inflate(inflater, container, false);
        appColorCollection = new AppColorCollection(inflater.getContext());
        applyFilter(filter);
        initFilterRow();
        initGraph(binding.lineChartView);
        setData(filter);
        return binding.getRoot();
    }

    private void initFilterRow() {
        binding.graphFilterButton.setOnClickListener((v) -> openFilter(this::updateFilter));
        binding.textView.setTextColor(getContext().getColor(R.color.foreground_color));
        if (showFilter) {
            binding.graphFilterButton.setVisibility(View.VISIBLE);
            binding.textView.setVisibility((View.VISIBLE));
        } else {
            binding.graphFilterButton.setVisibility(View.GONE);
            binding.textView.setVisibility((View.GONE));
        }
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
            List<Integer> colors = getColors();
            LineData data = new LineData();
            int i=0;
            for (Map.Entry<LocalDate, List<Entry>> entry : entriesMap.entrySet()) {
                List<Entry> entries = entry.getValue();
                LineDataSet barDataSet = new LineDataSet(entries, "Purchases");
                barDataSet.setDrawIcons(false);
                barDataSet.setColor(colors.get(i++));
                barDataSet.setLabel(entry.getKey().format(DATE_TIME_FORMATTER));
                data.addDataSet(barDataSet);
            }
            data.setValueTextColor(appColorCollection.getForegroundColor());
            notifyDataChanged(data);
        }).start();
    }

private List<Integer> getColors() {
    List<Integer> list = new ArrayList<>();
    list.add(getResources().getColor(R.color.line_chart_color_1, getContext().getTheme()));
    list.add(getResources().getColor(R.color.line_chart_color_2, getContext().getTheme()));
    list.add(getResources().getColor(R.color.line_chart_color_3, getContext().getTheme()));
    list.add(getResources().getColor(R.color.line_chart_color_4, getContext().getTheme()));
    list.add(getResources().getColor(R.color.line_chart_color_5, getContext().getTheme()));
    list.add(getResources().getColor(R.color.line_chart_color_6, getContext().getTheme()));
    list.add(getResources().getColor(R.color.line_chart_color_7, getContext().getTheme()));
    list.add(getResources().getColor(R.color.line_chart_color_8, getContext().getTheme()));
    list.add(getResources().getColor(R.color.line_chart_color_9, getContext().getTheme()));
    list.add(getResources().getColor(R.color.line_chart_color_10, getContext().getTheme()));
    list.add(getResources().getColor(R.color.line_chart_color_11, getContext().getTheme()));
    list.add(getResources().getColor(R.color.line_chart_color_12, getContext().getTheme()));
    return list;
}

    @NotNull
    private Map<LocalDate,List<Entry>> getEntries(CalendarReport calendarReport) {
        Map<LocalDate, List<Entry>> map = new HashMap<>();
        for (CalendarReportEntry calendarReportEntry : calendarReport.getContent()) {
            LocalDate key = calendarReportEntry.getLocalDate().withDayOfMonth(1);
            List<Entry> entries = map.computeIfAbsent(key,(k)->new ArrayList<>());
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

    private void openFilter(Consumer<PurchaseFilter> setFilter) {
        if (filterDialog.getFilter() == null)
            filterDialog.setFilter(getDefaultFilter());
        filterDialog.show(getParentFragmentManager(), "purchasesFilterDialog");
        filterDialog.setOnSuccess(setFilter);
    }

    private void updateFilter(PurchaseFilter newFilter) {
        this.filter = newFilter;
        this.applyFilter(newFilter);
        if (filterDialog.isAdded())
            filterDialog.dismiss();
        refresh(newFilter);
    }

    private void applyFilter(PurchaseFilter newFilter) {
        binding.graphFilterButton.setText(R.string.filterButton);
        binding.textView.setText(filter.getReadableString());
    }

    public void refresh(PurchaseFilter filter) {
        this.filter = filter;
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

    public String getDescription(CalendarReportEntry entry) {
        return getString(R.string.total_sum) + ": " + entry.getSum() + "\n" +
                getString(R.string.number_of_purchases) + ": " + entry.getCount();
    }

    @Override
    public void onNothingSelected() {

    }

    private void setTooltipText(String title, String text) {
        alertBuilder.setMessage(text).setTitle(title);
        AlertDialog dialog = alertBuilder.create();
        dialog.show();
    }
}