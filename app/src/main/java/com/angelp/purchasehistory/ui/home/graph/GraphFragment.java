package com.angelp.purchasehistory.ui.home.graph;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.angelp.purchasehistory.data.filters.PurchaseFilter;
import com.angelp.purchasehistory.data.interfaces.RefreshablePurchaseFragment;
import com.angelp.purchasehistory.databinding.FragmentGraphBinding;
import com.angelp.purchasehistory.ui.home.dashboard.pie.ColoredLabelXAxisRenderer;
import com.angelp.purchasehistory.ui.home.purchases.PurchaseFilterDialog;
import com.angelp.purchasehistory.util.AndroidUtils;
import com.angelp.purchasehistory.web.clients.PurchaseClient;
import com.angelp.purchasehistorybackend.models.views.outgoing.CategoryView;
import com.angelp.purchasehistorybackend.models.views.outgoing.analytics.CalendarReport;
import com.angelp.purchasehistorybackend.models.views.outgoing.analytics.CalendarReportEntry;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import dagger.hilt.android.AndroidEntryPoint;
import lombok.NoArgsConstructor;
import org.assertj.core.data.MapEntry;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.angelp.purchasehistory.data.Constants.getDefaultFilter;

@NoArgsConstructor
@AndroidEntryPoint
public class GraphFragment extends Fragment implements RefreshablePurchaseFragment {
    private static final String ARG_FILTER = "purchase_filter_graph";
    private final String TAG = this.getClass().getSimpleName();
    @Inject
    PurchaseClient purchaseClient;
    private PurchaseFilter filter = new PurchaseFilter();
    private PurchaseFilterDialog filterDialog = new PurchaseFilterDialog(true);
    private Consumer<PurchaseFilter> setFilter;

    private FragmentGraphBinding binding;

    public GraphFragment(PurchaseFilter filter, Consumer<PurchaseFilter> setFilter) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_FILTER, filter);
        this.setArguments(args);
        this.setFilter = setFilter;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            filter = getArguments().getParcelable(ARG_FILTER);
        }
    }
    @Override
    public void onSaveInstanceState(@NonNull @NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(ARG_FILTER, filter);
    }


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: View created");

        binding = FragmentGraphBinding.inflate(inflater, container, false);
        binding.graphFilterButton.setOnClickListener((v)-> openFilter(this::updateFilter));
        initGraph(binding.barChartView);
        setData(filter);
        return binding.getRoot();
    }

    private void initGraph(BarChart chart) {
        chart.setHighlightFullBarEnabled(true);
        chart.setFitBars(true);
        chart.setEnabled(true);
        chart.setElevation(5);
        chart.setHighlightPerTapEnabled(true);
        chart.getDescription().setEnabled(false);
        chart.setExtraOffsets(5, 10, 5, 5);
        chart.setDragDecelerationFrictionCoef(0.95f);
    }


    private void setData(PurchaseFilter filter) {
        new Thread(() -> {
            CalendarReport calendarReport = purchaseClient.getCalendarReport(filter);
            Map<Long, Map<LocalDate, CalendarReportEntry>> content = new HashMap<>();
            for (CalendarReportEntry calendarReportEntry : calendarReport.getContent()) {
                LocalDate dateIterator = filter.getFrom();
                Map<LocalDate, CalendarReportEntry> dayEntry = content.computeIfAbsent(calendarReportEntry.getCategory().getId(), (key) -> new HashMap<>());
                for (; dateIterator.isBefore(filter.getTo()); dateIterator = dateIterator.plusDays(1L))
                    dayEntry.put(dateIterator, new CalendarReportEntry(dateIterator.format(DateTimeFormatter.ISO_LOCAL_DATE), BigDecimal.ZERO, 0L, calendarReportEntry.getCategory()));
                dayEntry.put(calendarReportEntry.getLocalDate(), calendarReportEntry);
                content.put(calendarReportEntry.getCategory().getId(), dayEntry);
            }
            BarData data = new BarData();
            for(Map.Entry<Long, Map<LocalDate,CalendarReportEntry>> entry :content.entrySet()){
                List<BarEntry> entries = new ArrayList<>();
                int i = 0;
                CategoryView categoryView = new CategoryView();
                for (CalendarReportEntry dateEntry : entry.getValue().values()) {
                    entries.add(parseBarEntries(dateEntry, i));
                    categoryView = dateEntry.getCategory();
                    i++;
                }
                BarDataSet barDataSet = new BarDataSet(entries, categoryView.getName());
                int categoryColor = AndroidUtils.getColor(categoryView);
                barDataSet.setColor(categoryColor);
                barDataSet.setValueTextColor(categoryColor);
                barDataSet.setLabel(categoryView.getName());

                data.addDataSet(barDataSet);
                binding.barChartView.getXAxis().setValueFormatter(new ValueFormatter() {
                    @Override
                    public String getFormattedValue(float value) {
                        return entries.get(((Float)value).intValue()).getData().toString();
                    }
                });
            }
            new Handler(Looper.getMainLooper()).post(() -> {
                binding.barChartView.setData(data);
                binding.barChartView.notifyDataSetChanged();
                binding.barChartView.animateY(1000);
                binding.barChartView.invalidate();
            });
        }).start();


    }

    private BarEntry parseBarEntries(CalendarReportEntry entry, int index) {
        return new BarEntry(index, entry.getSum().floatValue(), entry.getDate());
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
        binding.graphFilterButton.setText(newFilter.isEmpty() ? "Filter" : "Filtered");
        binding.textView.setText(filter.getReadableString());
    }
    public void refresh(PurchaseFilter filter) {
        this.filter = filter;
        new Thread(() -> {
            setData(filter);
            getActivity().runOnUiThread(() -> {
                binding.barChartView.notifyDataSetChanged();
                binding.barChartView.animateY(1400, Easing.EaseInOutQuad);
            });
        }).start();
    }

}