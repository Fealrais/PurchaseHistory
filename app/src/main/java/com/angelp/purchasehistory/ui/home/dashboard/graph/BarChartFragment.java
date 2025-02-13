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
import com.angelp.purchasehistory.databinding.FragmentBarChartBinding;
import com.angelp.purchasehistory.ui.home.dashboard.RefreshableFragment;
import com.angelp.purchasehistory.ui.home.dashboard.purchases.PurchaseFilterDialog;
import com.angelp.purchasehistory.util.AndroidUtils;
import com.angelp.purchasehistory.web.clients.PurchaseClient;
import com.angelp.purchasehistorybackend.models.views.outgoing.CategoryView;
import com.angelp.purchasehistorybackend.models.views.outgoing.analytics.CalendarReport;
import com.angelp.purchasehistorybackend.models.views.outgoing.analytics.CalendarReportEntry;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.YAxis;
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
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.angelp.purchasehistory.data.Constants.getDefaultFilter;

@AndroidEntryPoint
public class BarChartFragment extends RefreshableFragment implements OnChartValueSelectedListener {
    private final String TAG = this.getClass().getSimpleName();
    private final PurchaseFilterDialog filterDialog = new PurchaseFilterDialog(true);
    @Inject
    PurchaseClient purchaseClient;
    AlertDialog.Builder alertBuilder;
    private FragmentBarChartBinding binding;
    private AlertDialog dialog;
    private boolean showFilter;
    private AppColorCollection appColorCollection;


    public BarChartFragment(PurchaseFilter filter, Consumer<PurchaseFilter> setFilter) {
        super(filter, setFilter);
        Bundle args = new Bundle();
        args.putParcelable(Constants.ARG_FILTER, filter);
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
            filter = getArguments().getParcelable(Constants.ARG_FILTER);
            showFilter = getArguments().getBoolean(Constants.SHOW_FILTER);
        } else {
            filter = new PurchaseFilter();
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
        binding = FragmentBarChartBinding.inflate(inflater, container, false);
        appColorCollection = new AppColorCollection(inflater.getContext());
        applyFilter(filter);
        initFilterRow();
        initGraph(binding.barChartView);
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

    private void initGraph(BarChart chart) {
        AndroidUtils.initChart(chart, appColorCollection, "dd MMM");
        if (showFilter) {
            chart.setOnChartValueSelectedListener(this);
        }

        // change the position of the y-labels
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setAxisMinimum(0f); // this replaces setStartAtZero(true)
        leftAxis.setTextColor(appColorCollection.getForegroundColor());
        YAxis rightAxis = chart.getAxisRight();
        rightAxis.setEnabled(false);
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
            if (!colors.isEmpty())
                barDataSet.setColors(colors);
            barDataSet.setStackLabels(labels.toArray(new String[0]));
            BarData data = new BarData(barDataSet);
            data.setValueTextColor(appColorCollection.getForegroundColor());
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
        binding.graphFilterButton.setText(R.string.filterButton);
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
        if (data == null || data.isEmpty())
            return;
        setTooltipText(data.get(0).getLocalDate().format(DateTimeFormatter.ofPattern("dd MMM yyyy")), data.stream()
                .filter(entry -> entry.getCount() > 0)
                .map(this::getDescription)
                .collect(Collectors.joining("\n\n")));
    }

    public String getDescription(CalendarReportEntry entry) {
        return entry.getCategory().getName() + ": " + entry.getSum() + "\n" +
                " - " + getString(R.string.number_of_purchases) + ": " + entry.getCount();
    }

    @Override
    public void onNothingSelected() {
        if (dialog != null && dialog.isShowing()) dialog.dismiss();
    }

    private void setTooltipText(String title, String text) {
        if (dialog != null && dialog.isShowing()) dialog.dismiss();

        alertBuilder.setMessage(text).setTitle(title);
        dialog = alertBuilder.create();
        dialog.show();
    }
}