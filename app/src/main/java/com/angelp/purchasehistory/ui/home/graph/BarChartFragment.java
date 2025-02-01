package com.angelp.purchasehistory.ui.home.graph;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.data.filters.PurchaseFilter;
import com.angelp.purchasehistory.data.interfaces.RefreshablePurchaseFragment;
import com.angelp.purchasehistory.databinding.FragmentBarChartBinding;
import com.angelp.purchasehistory.ui.home.purchases.PurchaseFilterDialog;
import com.angelp.purchasehistory.util.AndroidUtils;
import com.angelp.purchasehistory.web.clients.PurchaseClient;
import com.angelp.purchasehistorybackend.models.views.outgoing.CategoryView;
import com.angelp.purchasehistorybackend.models.views.outgoing.analytics.CalendarReport;
import com.angelp.purchasehistorybackend.models.views.outgoing.analytics.CalendarReportEntry;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import dagger.hilt.android.AndroidEntryPoint;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.angelp.purchasehistory.data.Constants.getDefaultFilter;

@NoArgsConstructor
@AndroidEntryPoint
public class BarChartFragment extends Fragment implements RefreshablePurchaseFragment, OnChartValueSelectedListener {
    private static final String ARG_FILTER = "purchase_filter_graph";
    private final String TAG = this.getClass().getSimpleName();
    private final PurchaseFilterDialog filterDialog = new PurchaseFilterDialog(true);
    @Inject
    PurchaseClient purchaseClient;
    AlertDialog.Builder alertBuilder;
    private PurchaseFilter filter;
    private Consumer<PurchaseFilter> setFilter;
    private FragmentBarChartBinding binding;


    public BarChartFragment(PurchaseFilter filter, Consumer<PurchaseFilter> setFilter) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_FILTER, filter);
        this.setArguments(args);
        this.setFilter = setFilter;
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
            filter = getArguments().getParcelable(ARG_FILTER);
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
        outState.putParcelable(ARG_FILTER, filter);
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: View created");

        binding = FragmentBarChartBinding.inflate(inflater, container, false);
        applyFilter(filter);
        binding.graphFilterButton.setOnClickListener((v) -> openFilter(this::updateFilter));
        binding.textView.setTextColor(Color.BLACK);
        initGraph(binding.barChartView);
        setData(filter);
        return binding.getRoot();
    }

    private void initGraph(BarChart chart) {
        chart.setDrawValueAboveBar(true);
        chart.setExtraOffsets(5, 10, 5, 5);
        chart.setDragDecelerationFrictionCoef(0.95f);
        chart.setBackgroundColor(Color.WHITE);
        chart.setHighlightPerTapEnabled(true);
        chart.getDescription().setEnabled(false);
        chart.setOnChartValueSelectedListener(this);
        chart.getAxisRight().setEnabled(false);

        DayAxisValueFormatter xAxisFormatter = new DayAxisValueFormatter();

        XAxis xLabels = chart.getXAxis();
        xLabels.setPosition(XAxis.XAxisPosition.BOTTOM);
        xLabels.setValueFormatter(xAxisFormatter);
        xLabels.setGranularity(1f);

        // change the position of the y-labels
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)

        Legend l = chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(true);
//        l.setFormSize(8f);
//        l.setFormToTextSpace(4f);
//        l.setXEntrySpace(6f);

    }

    private void setData(PurchaseFilter filter) {
        new Thread(() -> {
            CalendarReport calendarReport = purchaseClient.getCategorizedCalendarReport(filter);
            List<CategoryView> allCategories = purchaseClient.getAllCategories();

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
            barDataSet.setDrawIcons(false);
            if(!colors.isEmpty())
                barDataSet.setColors(colors);
            barDataSet.setStackLabels(labels.toArray(new String[0]));
            BarData data = new BarData(barDataSet);
            data.setBarWidth(0.95f);
            notifyDataChanged(data);
        }).start();
    }

    private void notifyDataChanged(BarData data) {
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

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        if (e == null)
            return;
        List<CalendarReportEntry> data = (List<CalendarReportEntry>) e.getData();
        if(data == null || data.isEmpty())
            return;
        setTooltipText(data.get(0).getLocalDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")), data.stream()
                .filter(entry -> entry.getCount() > 0)
                .map(this::getDescription)
                .collect(Collectors.joining("\n\n")));
    }

    public String getDescription(CalendarReportEntry entry) {
        return entry.getCategory().getName() + ": " + entry.getSum() + "\n" +
                " - "+getString(R.string.number_of_purchases)+": " + entry.getCount();
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