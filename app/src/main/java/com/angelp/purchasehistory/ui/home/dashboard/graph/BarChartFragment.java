package com.angelp.purchasehistory.ui.home.dashboard.graph;

import android.app.AlertDialog;
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
import com.angelp.purchasehistory.databinding.FragmentBarChartBinding;
import com.angelp.purchasehistory.ui.home.dashboard.purchases.PurchaseFilterDialog;
import com.angelp.purchasehistory.util.AndroidUtils;
import com.angelp.purchasehistory.web.clients.PurchaseClient;
import com.angelp.purchasehistorybackend.models.views.outgoing.CategoryView;
import com.angelp.purchasehistorybackend.models.views.outgoing.analytics.CalendarReport;
import com.angelp.purchasehistorybackend.models.views.outgoing.analytics.CalendarReportEntry;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import dagger.hilt.android.AndroidEntryPoint;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AndroidEntryPoint
public class BarChartFragment extends RefreshablePurchaseFragment implements OnChartValueSelectedListener {
    private final String TAG = this.getClass().getSimpleName();
    private final PurchaseFilterDialog filterDialog = new PurchaseFilterDialog(true);
    @Inject
    PurchaseClient purchaseClient;
    AlertDialog.Builder alertBuilder;
    private FragmentBarChartBinding binding;
    private PurchasesPerDayDialog dialog;
    private boolean showFilter;
    private AppColorCollection appColorCollection;
    private Typeface tf;

    public BarChartFragment() {
        Bundle args = new Bundle();
        this.setArguments(args);
    }

    private static Map<LocalDate, List<CalendarReportEntry>> prepareContent(PurchaseFilter filter, CalendarReport calendarReport, List<CategoryView> categories) {
        Map<LocalDate, List<CalendarReportEntry>> content = new HashMap<>();
        LocalDate dateIterator = filter.getFrom();
        for (; dateIterator.isBefore(filter.getTo().plusDays(1)); dateIterator = dateIterator.plusDays(1L)) {
            List<CalendarReportEntry> list = content.computeIfAbsent(dateIterator, (key) -> new ArrayList<>());

            for (CategoryView category : categories) {
                boolean added = false;
                for (CalendarReportEntry calendarReportEntry : calendarReport.getContent()) {
                    if (calendarReportEntry.getLocalDate().equals(dateIterator) && calendarReportEntry.getCategory().getId().equals(category.getId())) {
                        list.add(calendarReportEntry);
                        added = true;
                        break;
                    }
                }
                if (!added) {
                    list.add(new CalendarReportEntry(dateIterator.format(DateTimeFormatter.ISO_LOCAL_DATE), BigDecimal.ZERO, 0L, new CategoryView()));
                }
            }
            content.put(dateIterator, list);
        }
        return content;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            showFilter = getArguments().getBoolean(Constants.Arguments.ARG_SHOW_FILTER);
        }
        alertBuilder = new AlertDialog.Builder(getActivity());
    }

    @Override
    public void onSaveInstanceState(@NonNull @NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(Constants.Arguments.ARG_SHOW_FILTER, showFilter);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: View created");
        binding = FragmentBarChartBinding.inflate(inflater, container, false);
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
        initGraph(binding.barChartView);
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

    private void initGraph(BarChart chart) {
        AndroidUtils.initChart(chart, appColorCollection, "dd MMM", tf);
        chart.setOnChartValueSelectedListener(this);
        chart.setNoDataText(getString(R.string.no_data));
    }

    private void setData(PurchaseFilter filter) {
        new Thread(() -> {
            isRefreshing.postValue(true);
            CalendarReport calendarReport = purchaseClient.getCategorizedCalendarReport(filter);
            List<CategoryView> allCategories = purchaseClient.getAllCategories();
            if (!calendarReport.getContent().isEmpty()) {
                updateChart(filter, calendarReport, allCategories);
            }
            isRefreshing.postValue(false);
        }).start();
    }

    private void updateChart(PurchaseFilter filter, CalendarReport calendarReport, List<CategoryView> allCategories) {
        Map<LocalDate, List<CalendarReportEntry>> content = prepareContent(filter, calendarReport, allCategories);
        List<String> labels = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();
        List<BarEntry> entries = new ArrayList<>();

        for (CategoryView allCategory : allCategories) {
            labels.add(allCategory.getName());
            int color = AndroidUtils.getColor(allCategory);
            colors.add(color);
        }
        LocalDate dateIterator = filter.getFrom();
        for (; dateIterator.isBefore(filter.getTo().plusDays(1)); dateIterator = dateIterator.plusDays(1L)) {
            entries.add(parseBarEntries(dateIterator, content.get(dateIterator)));
        }
        BarDataSet barDataSet = new BarDataSet(entries, "Purchases");
        barDataSet.setValueTypeface(tf);
        barDataSet.setDrawIcons(false);
        if (!colors.isEmpty())
            barDataSet.setColors(colors);
        barDataSet.setStackLabels(labels.toArray(new String[0]));
        BarData data = new BarData(barDataSet);
        data.setValueTextColor(appColorCollection.getForegroundColor());
        data.setBarWidth(0.95f);
        notifyDataChanged(data);
    }

    private void notifyDataChanged(BarData data) {
        if (data == null || data.getDataSetCount() == 0) return;

        new Handler(Looper.getMainLooper()).post(() -> {
            binding.barChartView.setData(data);
            binding.barChartView.getData().notifyDataChanged();
            binding.barChartView.notifyDataSetChanged();
            binding.barChartView.animateY(1000);
            binding.barChartView.invalidate();
        });
    }

    private BarEntry parseBarEntries(LocalDate date, List<CalendarReportEntry> entries) {
        float[] list = new float[entries.size()];
        float x = ((Long) date.toEpochDay()).floatValue();
        for (int i = 0; i < entries.size(); i++) {
            CalendarReportEntry e = entries.get(i);
            float floatValue = e.getSum().floatValue();
            list[i] = floatValue;
        }
        return new BarEntry(x, list, entries);
    }

    private void openFilter() {
        filterDialog.show(getParentFragmentManager(), "barchartFilterDialog");
    }

    private void applyFilter(PurchaseFilter newFilter) {
        binding.graphFilterButton.setText(R.string.filterButton);
        binding.textView.setText(newFilter.getReadableString());
    }

    public void refresh(PurchaseFilter filter) {
        if (binding == null) return;
        applyFilter(filter);
        new Thread(() -> {
            setData(filter);
        }).start();
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        if (e == null)
            return;
        List<CalendarReportEntry> data = (List<CalendarReportEntry>) e.getData();
        if (data == null || data.isEmpty())
            return;

        if (dialog != null && dialog.isAdded()) dialog.dismiss();
        BigDecimal sum = BigDecimal.ZERO;
        for (CalendarReportEntry datum : data) {
            sum = sum.add(datum.getSum());
        }
        if (!sum.equals(BigDecimal.ZERO)) {
            dialog = new PurchasesPerDayDialog(data.get(0));
            dialog.show(getParentFragmentManager().beginTransaction(), "DialogFragment");
        }

    }

    @Override
    public void onNothingSelected() {
        if (dialog != null && dialog.isAdded()) dialog.dismiss();
    }
}