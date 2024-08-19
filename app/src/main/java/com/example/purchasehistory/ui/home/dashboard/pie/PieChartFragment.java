package com.example.purchasehistory.ui.home.dashboard.pie;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.angelp.purchasehistorybackend.models.views.outgoing.CategoryView;
import com.angelp.purchasehistorybackend.models.views.outgoing.analytics.CategoryAnalyticsEntry;
import com.angelp.purchasehistorybackend.models.views.outgoing.analytics.CategoryAnalyticsReport;
import com.example.purchasehistory.data.filters.PurchaseFilter;
import com.example.purchasehistory.data.interfaces.RefreshablePurchaseFragment;
import com.example.purchasehistory.databinding.FragmentPieChartBinding;
import com.example.purchasehistory.ui.home.dashboard.DashboardViewModel;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.MPPointF;
import dagger.hilt.android.AndroidEntryPoint;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@NoArgsConstructor
@AndroidEntryPoint
public class PieChartFragment extends Fragment implements OnChartValueSelectedListener, RefreshablePurchaseFragment {
    private static final String ARG_FILTER = "purchase_filter";
    private final String TAG = this.getClass().getSimpleName();

    private PurchaseFilter filter;
    private Consumer<PurchaseFilter> setFilter;
    private DashboardViewModel viewModel;
    private FragmentPieChartBinding binding;

    public PieChartFragment(PurchaseFilter filter, Consumer<PurchaseFilter> setFilter) {
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
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        binding = FragmentPieChartBinding.inflate(inflater, container, false);
        initPieGraph();
        return binding.getRoot();
    }

    private void initPieGraph() {
        initPieChart(binding.pieChart);
        new Thread(() -> setData(filter)).start();
    }

    private void setData(PurchaseFilter filter) {
        CategoryAnalyticsReport report = viewModel.getCategoryAnalyticsReport(filter);
        List<PieEntry> entries = report.getContent().stream().map(this::parsePieEntries).collect(Collectors.toList());
        PieDataSet dataSet = new PieDataSet(entries, "Category");
        binding.pieChart.setCenterText(String.format(Locale.ENGLISH, "All Purchases\nSum: %.2f", report.getTotalSum()));
        dataSet.setDrawIcons(false);
        dataSet.setSliceSpace(3f);
        dataSet.setIconsOffset(new MPPointF(0, 40));
        dataSet.setSelectionShift(5f);
        dataSet.setColors(report.getContent().stream().map(entry -> {
                    if (entry.getCategory() != null && entry.getCategory().getColor() != null && !entry.getCategory().getColor().isBlank())
                        return Color.parseColor(entry.getCategory().getColor());
                    else return Color.GRAY;
                }
        ).collect(Collectors.toList()));
        dataSet.setValueTextSize(11f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.ENGLISH, "%.2f", value);
            }
        });

        PieData newData = new PieData(dataSet);

        new Handler(Looper.getMainLooper()).post(() -> {
            binding.pieChart.setData(newData);
            binding.pieChart.notifyDataSetChanged();
            binding.pieChart.animateY(1000);
            binding.pieChart.invalidate();
        });
    }

    private PieEntry parsePieEntries(CategoryAnalyticsEntry entry) {
        String name = entry.getCategory() != null && !entry.getCategory().getName().isBlank()? entry.getCategory().getName(): "Unknown";
        return new PieEntry(entry.getSum().floatValue(), name, entry.getCategory());
    }

    private void initPieChart(PieChart chart) {
        chart.setUsePercentValues(false);
        chart.getDescription().setEnabled(false);
        chart.setExtraOffsets(5, 10, 5, 5);

        chart.setDragDecelerationFrictionCoef(0.95f);
        chart.setDrawEntryLabels(true);

        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(Color.WHITE);

        chart.setTransparentCircleColor(Color.WHITE);
        chart.setTransparentCircleAlpha(110);

        chart.setHoleRadius(58f);
        chart.setTransparentCircleRadius(61f);

        chart.setDrawCenterText(true);
        chart.setCenterTextSize(15f);
        chart.setRotationAngle(0);
        chart.setElevation(5);
        // enable rotation of the chart by touch
        chart.setRotationEnabled(true);
        chart.setHighlightPerTapEnabled(true);

        // add a selection listener
        chart.setOnChartValueSelectedListener(this);

        Legend l = chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);

        // entry label styling
        chart.setEntryLabelColor(Color.WHITE);
        chart.setEntryLabelTextSize(12f);
    }


    @Override
    public void onValueSelected(Entry e, Highlight h) {

        if (e == null)
            return;
        Log.i(TAG, String.format("Selected value: %s, index: %s, DataSet index: %d", e.getY(), h.getX(), h.getDataSetIndex()));
        CategoryView category = (CategoryView) e.getData();
        filter.setCategoryId(category.getId());
        setFilter.accept(filter);
    }

    @Override
    public void onNothingSelected() {
        Log.i(TAG, "nothing selected");
        filter.setCategoryId(null);
        setFilter.accept(filter);
    }

    public void refresh(PurchaseFilter filter) {
        this.filter = filter;
        new Thread(() -> {
            setData(filter);
            getActivity().runOnUiThread(() -> {
                binding.pieChart.notifyDataSetChanged();
                binding.pieChart.animateY(1400, Easing.EaseInOutQuad);
            });
        }).start();
    }
}