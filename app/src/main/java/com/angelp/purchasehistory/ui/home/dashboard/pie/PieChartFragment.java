package com.angelp.purchasehistory.ui.home.dashboard.pie;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.lifecycle.ViewModelProvider;
import com.angelp.purchasehistory.R;
import com.angelp.purchasehistory.data.AppColorCollection;
import com.angelp.purchasehistory.data.Constants;
import com.angelp.purchasehistory.data.filters.PurchaseFilter;
import com.angelp.purchasehistory.databinding.FragmentPieChartBinding;
import com.angelp.purchasehistory.ui.home.dashboard.DashboardViewModel;
import com.angelp.purchasehistory.ui.home.dashboard.RefreshableFragment;
import com.angelp.purchasehistory.ui.home.dashboard.purchases.PurchaseFilterDialog;
import com.angelp.purchasehistory.util.AndroidUtils;
import com.angelp.purchasehistorybackend.models.views.outgoing.CategoryView;
import com.angelp.purchasehistorybackend.models.views.outgoing.analytics.CategoryAnalyticsEntry;
import com.angelp.purchasehistorybackend.models.views.outgoing.analytics.CategoryAnalyticsReport;
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

import static com.angelp.purchasehistory.data.Constants.getDefaultFilter;

@AndroidEntryPoint
@NoArgsConstructor
public class PieChartFragment extends RefreshableFragment implements OnChartValueSelectedListener {
    private static final String ARG_FILTER = "purchase_filter";
    private final String TAG = this.getClass().getSimpleName();
    private final PurchaseFilterDialog filterDialog = new PurchaseFilterDialog(false);
    private DashboardViewModel viewModel;
    private FragmentPieChartBinding binding;
    private boolean showFilter;
    private AppColorCollection appColorCollection;

    public PieChartFragment(PurchaseFilter filter, Consumer<PurchaseFilter> setFilter) {
        super(filter, setFilter);
        Bundle args = new Bundle();
        args.putParcelable(ARG_FILTER, filter);
        this.setArguments(args);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            showFilter = getArguments().getBoolean(Constants.ARG_SHOW_FILTER);
            filter = getArguments().getParcelable(ARG_FILTER);
        }
    }

    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        binding = FragmentPieChartBinding.inflate(inflater, container, false);
        appColorCollection = new AppColorCollection(inflater.getContext());
        applyFilter(filter);
        initFilterRow();
        initPieChart(binding.pieChart);
        new Thread(() -> setData(filter)).start();
        return binding.getRoot();
    }

    private void applyFilter(PurchaseFilter newFilter) {
        binding.piechartFilterButton.setText(R.string.filterButton);
        binding.textView.setText(newFilter.getReadableString());
    }
    private void initFilterRow() {
        binding.piechartFilterButton.setOnClickListener((v) -> openFilter(this::updateFilter));
        binding.textView.setTextColor(getContext().getColor(R.color.foreground_color));
        new Handler(Looper.getMainLooper()).post(() -> {
            binding.piechartFilterButton.setVisibility(showFilter?View.VISIBLE:View.GONE);
            binding.textView.setVisibility(showFilter?View.VISIBLE:View.GONE);
        });
    }
    private void openFilter(Consumer<PurchaseFilter> setFilter) {
        if (filterDialog.getFilter() == null)
            filterDialog.setFilter(getDefaultFilter());
        filterDialog.show(getParentFragmentManager(), "piechartFilterDialog");
        filterDialog.setOnSuccess(setFilter);
    }

    private void updateFilter(PurchaseFilter newFilter) {
        this.filter = newFilter;
        this.applyFilter(newFilter);
        if (filterDialog.isAdded())
            filterDialog.dismiss();
        refresh(newFilter);
    }

    private void setData(PurchaseFilter filter) {
        CategoryAnalyticsReport report = viewModel.getCategoryAnalyticsReport(filter);
        if (report == null) {
            binding.pieChart.setCenterText("Failed to load data.\nTry again later.");
            return;
        }
        List<PieEntry> entries = report.getContent().stream().map(this::parsePieEntries).collect(Collectors.toList());
        PieDataSet dataSet = new PieDataSet(entries, "Category");

        List<Integer> categoryColors = report.getContent().stream().map(entry -> AndroidUtils.getColor(entry.getCategory())
        ).collect(Collectors.toList());
        dataSet.setAutomaticallyDisableSliceSpacing(true);
        String centerText = report.getTotalSum() == null ? getString(R.string.no_data) :  getString(R.string.pie_chart_sum, report.getTotalSum());
        binding.pieChart.setCenterText(centerText);
        dataSet.setDrawIcons(false);
        dataSet.setSliceSpace(3f);
        dataSet.setIconsOffset(new MPPointF(0, 40));
        dataSet.setSelectionShift(5f);
        dataSet.setColors(categoryColors);
        dataSet.setValueTextColors(categoryColors.stream().map(AndroidUtils::getTextColor).collect(Collectors.toList()));
        dataSet.setValueTextSize(11f);

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
        String name = entry.getCategory() != null && !entry.getCategory().getName().isBlank() ? entry.getCategory().getName() : "Unknown";
        return new PieEntry(entry.getSum().floatValue(), name, entry.getCategory());
    }

    private void initPieChart(PieChart chart) {
        chart.setUsePercentValues(false);
        chart.getDescription().setEnabled(false);
        chart.setExtraOffsets(5, 10, 5, 5);

        chart.setDragDecelerationFrictionCoef(0.95f);
        chart.setDrawEntryLabels(true);

        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(appColorCollection.getForegroundColor());

        chart.setTransparentCircleColor(appColorCollection.getForegroundColor());
//        chart.setTransparentCircleAlpha(50);

//        chart.setHoleRadius(58f);
//        chart.setTransparentCircleRadius(61f);
        chart.setDrawCenterText(true);
        chart.setCenterTextSize(15f);
        chart.setRotationAngle(0);
        chart.setElevation(5);
        // enable rotation of the chart by touch
        chart.setRotationEnabled(true);
        chart.setHighlightPerTapEnabled(true);
        chart.setRenderer(new ColoredLabelXAxisRenderer(chart, chart.getAnimator(), chart.getViewPortHandler()));

        // add a selection listener
        chart.setOnChartValueSelectedListener(this);

        Legend l = chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setTextColor(appColorCollection.getForegroundColor());
        l.setDrawInside(false);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);

        // entry label styling
        chart.setEntryLabelColor(appColorCollection.getForegroundColor());
//        chart.setEntryLabelTextSize(12f);
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